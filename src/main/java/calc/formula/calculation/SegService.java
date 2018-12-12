package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.*;
import calc.entity.calc.seg.*;
import calc.formula.*;
import calc.formula.exception.CalcServiceException;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import static calc.entity.calc.enums.TreatmentTypeEnum.*;
import static calc.util.Util.buildMsgParams;
import static calc.util.Util.round;
import static java.util.Collections.emptyList;
import static java.util.Optional.*;
import static java.util.stream.Collectors.groupingBy;

@SuppressWarnings("ImplicitSubclassInspection")
@Service
@RequiredArgsConstructor
public class SegService {
    private static final Logger logger = LoggerFactory.getLogger(SegService.class);
    private static final String docCode = "SEG";
    private final SegResultHeaderRepo segResultHeaderRepo;
    private final SegResultLineRepo segResultLineRepo;
    private final SegResultNoteRepo segResultNoteRepo;
    private final SegResultAppRepo segResultAppRepo;
    private final MessageService messageService;
    private final CalcService calcService;

    public boolean calc(Long headerId) {
        logger.info("Seg calculation for header " + headerId + " started");
        SegResultHeader header = segResultHeaderRepo.findOne(headerId);
        if (header.getStatus() != BatchStatusEnum.W)
            return false;

        if (header.getDataType() == null)
            header.setDataType(header.getPeriodType() == PeriodTypeEnum.M ? DataTypeEnum.FINAL : DataTypeEnum.OPER);

        CalcContext context = CalcContext.builder()
            .lang(LangEnum.RU)
            .header(header)
            .defContextType(ContextTypeEnum.SEG)
            .build();

        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header);

            calcLines(header, context);
            setParents(header);
            copyNotes(header);
            copyApps(header);

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);

            updateStatus(header, BatchStatusEnum.C);
            logger.info("Seg calculation for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            logger.error("Seg calculation for header " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();

            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", buildMsgParams(e));
            updateStatus(header, BatchStatusEnum.E);
            return false;
        }
    }

    private void calcLines(SegResultHeader header, CalcContext context) {
        Map<TreatmentTypeEnum, List<BalanceUnitLine>> map = header.getBalanceUnit()
            .getLines()
            .stream()
            .filter(t -> t.getTreatmentType() != null)
            .collect(groupingBy(BalanceUnitLine::getTreatmentType));

        for (TreatmentTypeEnum treatmentType : Arrays.asList(INFO, IN, EMPTY, OUT)) {
            List<SegResultLine> results = new ArrayList<>();
            for (BalanceUnitLine line : map.getOrDefault(treatmentType, emptyList())) {
                Map<String, String> msgParams = buildMsgParams(line);
                MeteringPoint meteringPoint = line.getMeteringPoint();
                Parameter param = line.getParam();
                Formula formula = line.getFormula();

                SegResultLine result = new SegResultLine();
                copyTranslates(line, result);
                results.add(result);

                result.setHeader(header);
                result.setLineNum(line.getLineNum());
                result.setRowType(line.getRowType());
                result.setTreatmentType(line.getTreatmentType());
                result.setDataLocation(line.getDataLocation());
                result.setMeteringPoint(line.getMeteringPoint());
                result.setParam(line.getParam());
                result.setFormula(line.getFormula());
                result.setRate(line.getRate());
                result.setUnit(line.getParam().getUnit());
                result.setIsBold(line.getIsBold());
                result.setIsInverse(line.getIsInverse());
                result.setCreateBy(header.getCreateBy());
                result.setCreateDate(LocalDateTime.now());

                if (treatmentType == INFO)
                    continue;

                if (meteringPoint.getPointType() == PointTypeEnum.VMP && formula == null)
                    messageService.addMessage(header, line.getLineNum(), docCode, "SEG_FORMULA_NOT_FOUND", msgParams);

                Double val = null;
                try {
                    CalcProperty property = treatmentType == IN || formula == null
                        ? CalcProperty.builder()
                            .contextType(ContextTypeEnum.DEFAULT)
                            .processOrder(ProcessOrderEnum.READ)
                            .build()

                        : CalcProperty.builder()
                            .contextType(context.getDefContextType())
                            .processOrder(ProcessOrderEnum.CALC)
                            .build();

                    CalcResult calc;
                    if (formula != null)
                        calc = calcService.calcValue(formula, context, property);
                    else
                        calc = calcService.calcValue(meteringPoint, param, context, property);

                    val = result != null ? calc.getDoubleValue() : null;
                    if (val != null)
                        val = val * ofNullable(line.getRate()).orElse(1d);

                    val = round(val, param);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                }
                result.setVal(val);
            }
            saveLines(results);
        }
    }

    private void copyTranslates(BalanceUnitLine l, SegResultLine rl) {
        rl.setTranslates(ofNullable(rl.getTranslates()).orElse(new ArrayList<>()));
        for (BalanceUnitLineTranslate tl : l.getTranslates()) {
            SegResultLineTranslate rtl = new SegResultLineTranslate();
            rtl.setLang(tl.getLang());
            rtl.setLine(rl);
            rtl.setName(tl.getName());
            rtl.setShortName(tl.getShortName());

            if (rtl.getName() == null && rl.getMeteringPoint()!=null)
                rtl.setName(rl.getMeteringPoint().getShortName());

            rl.getTranslates().add(rtl);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void copyNotes(SegResultHeader header) {
        List<SegResultNote> resultNotes = new ArrayList<>();
        for (SegNote note : header.getHeader().getNotes()) {
            SegResultNote resultNote = new SegResultNote();
            resultNote.setHeader(header);
            resultNote.setLineNum(note.getLineNum());
            resultNote.setNoteNum(note.getNoteNum());

            resultNote.setTranslates(ofNullable(resultNote.getTranslates()).orElse(new ArrayList<>()));
            for (SegNoteTranslate noteTranslate : note.getTranslates()) {
                SegResultNoteTranslate resultNoteTranslate = new SegResultNoteTranslate();
                resultNoteTranslate.setNote(resultNote);
                resultNoteTranslate.setLang(noteTranslate.getLang());
                resultNoteTranslate.setNoteText(noteTranslate.getNoteText());
                resultNote.getTranslates().add(resultNoteTranslate);
            }
            resultNotes.add(resultNote);
        }
        segResultNoteRepo.save(resultNotes);
        segResultNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void copyApps(SegResultHeader header) {
        List<SegResultApp> resultApps = new ArrayList<>();
        for (SegApp app : header.getHeader().getApps()) {
            SegResultApp resultApp = new SegResultApp();
            resultApp.setHeader(header);
            resultApp.setAppNum(app.getAppNum());

            resultApp.setTranslates(ofNullable(resultApp.getTranslates()).orElse(new ArrayList<>()));
            for (SegAppTranslate appTranslate : app.getTranslates()) {
                SegResultAppTranslate resultAppTranslate = new SegResultAppTranslate();
                resultAppTranslate.setApp(resultApp);
                resultAppTranslate.setLang(appTranslate.getLang());
                resultAppTranslate.setAppName(appTranslate.getAppName());
                resultApp.getTranslates().add(resultAppTranslate);
            }
            resultApps.add(resultApp);
        }
        segResultAppRepo.save(resultApps);
        segResultAppRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void saveLines(List<SegResultLine> lines) {
        segResultLineRepo.save(lines);
        segResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void deleteLines(SegResultHeader header) {
        List<SegResultLine> lines = segResultLineRepo.findAllByHeaderId(header.getId());
        segResultLineRepo.delete(lines);
        segResultLineRepo.flush();

        List<SegResultNote> notes = segResultNoteRepo.findAllByHeaderId(header.getId());
        segResultNoteRepo.delete(notes);
        segResultNoteRepo.flush();

        List<SegResultApp> apps = segResultAppRepo.findAllByHeaderId(header.getId());
        segResultAppRepo.delete(apps);
        segResultAppRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    public void deleteMessages(SegResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void updateStatus(SegResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        segResultHeaderRepo.save(header);
        segResultHeaderRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void setParents(SegResultHeader header) {
        List<SegResultLine> lines = segResultLineRepo.findAllByHeaderId(header.getId());
        List<BalanceUnitLine> balanceUnitLines = header.getBalanceUnit().getLines();

        for (BalanceUnitLine balanceUnitLine : balanceUnitLines) {
            if (balanceUnitLine.getParent() == null)
                continue;

            SegResultLine line = lines.stream()
                .filter(t -> t.getLineNum().equals(balanceUnitLine.getLineNum()))
                .findFirst()
                .orElse(null);

            SegResultLine parentLine = lines.stream()
                .filter(t -> t.getLineNum().equals(balanceUnitLine.getParent().getLineNum()))
                .findFirst()
                .orElse(null);

            if (line != null && parentLine!= null)
                line.setParent(parentLine);
        }

        segResultLineRepo.save(lines);
        segResultLineRepo.flush();
    }
}
