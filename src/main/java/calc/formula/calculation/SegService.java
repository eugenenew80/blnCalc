package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.PointTypeEnum;
import calc.entity.calc.enums.TreatmentTypeEnum;
import calc.entity.calc.seg.*;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextType;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

import static calc.util.Util.round;

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
    private final ParamService paramService;
    private final PeriodTimeValueService periodTimeValueService;
    private final CalcService calcService;
    private Map<String, Parameter> mapParams = null;

    @PostConstruct
    public void init() {
        mapParams = paramService.getValues();
    }

    public boolean calc(Long headerId) {
        logger.info("Metering reading for header " + headerId + " started");
        SegResultHeader header = segResultHeaderRepo.findOne(headerId);
        if (header.getStatus() == BatchStatusEnum.E)
            return false;

        CalcContext context = CalcContext.builder()
            .docCode(docCode)
            .headerId(header.getId())
            .periodType(header.getPeriodType())
            .startDate(header.getStartDate())
            .endDate(header.getEndDate())
            .orgId(header.getOrganization().getId())
            .contextType(ContextType.SEG)
            .build();

        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header);

            copyInfoRows(header);
            calcInRows(header, context);
            calcOutRows(header, context);
            setParents(header);
            copyNotes(header);
            copyApps(header);

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);

            updateStatus(header, BatchStatusEnum.C);
            logger.info("Metering reading for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", new HashMap<>());
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void calcOutRows(SegResultHeader header, CalcContext context) throws Exception {
        List<SegResultLine> resultLines = new ArrayList<>();
        for (BalanceUnitLine line : header.getBalanceUnit().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.OUT && line.getTreatmentType() != TreatmentTypeEnum.EMPTY)
                continue;

            Map<String, String> msgParams = buildMsgParams(line);

            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();
            Formula formula = line.getFormula();

            if (meteringPoint == null) {
                messageService.addMessage(header, line.getLineNum(), docCode, "SEG_MP_NOT_FOUND", msgParams);
                continue;
            }

            if (param == null) {
                messageService.addMessage(header, line.getLineNum(), docCode, "SEG_PARAM_NOT_FOUND", msgParams);
                continue;
            }

            if (formula == null)
                messageService.addMessage(header, line.getLineNum(), docCode, "SEG_FORMULA_NOT_FOUND", msgParams);

            if (meteringPoint.getPointType() != PointTypeEnum.VMP)
                messageService.addMessage(header, line.getLineNum(), docCode, "SEG_NOT_VMP", msgParams);

            SegResultLine resultLine = new SegResultLine();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setRowType(line.getRowType());
            resultLine.setTreatmentType(line.getTreatmentType());
            resultLine.setDataLocation(line.getDataLocation());
            resultLine.setMeteringPoint(meteringPoint);
            resultLine.setParam(param);
            resultLine.setFormula(formula);
            resultLine.setRate(line.getRate());
            resultLine.setUnit(param.getUnit());
            resultLine.setCreateDate(LocalDateTime.now());
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setIsBold(line.getIsBold());
            resultLine.setIsInverse(line.getIsInverse());

            if (meteringPoint.getPointType() == PointTypeEnum.VMP && formula != null) {
                CalcResult result = calcService.calcMeteringPoint(formula, context);
                Double value = result != null ? result.getDoubleValue() : null;
                if (value != null)
                    value = value * Optional.ofNullable(line.getRate()).orElse(1d);

                value = round(value, param);
                resultLine.setVal(value);
            }
            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines(resultLines);
    }

    private void calcInRows(SegResultHeader header, CalcContext context) {
        List<SegResultLine> resultLines = new ArrayList<>();
        for (BalanceUnitLine line : header.getBalanceUnit().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.IN)
                continue;

            Map<String, String> msgParams = buildMsgParams(line);
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();

            if (meteringPoint == null) {
                messageService.addMessage(header, line.getLineNum(), docCode, "SEG_MP_NOT_FOUND", msgParams);
                continue;
            }

            if (param == null) {
                messageService.addMessage(header, line.getLineNum(), docCode, "SEG_PARAM_NOT_FOUND", msgParams);
                continue;
            }

            PeriodTimeValueExpression expression = PeriodTimeValueExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .periodType(context.getPeriodType())
                .rate(Optional.ofNullable(line.getRate()).orElse(1d))
                .startHour((byte) 0)
                .endHour((byte) 23)
                .service(periodTimeValueService)
                .context(context)
                .build();

            Double value = expression.doubleValue();
            value = round(value, param);

            SegResultLine resultLine = new SegResultLine();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setRowType(line.getRowType());
            resultLine.setTreatmentType(line.getTreatmentType());
            resultLine.setDataLocation(line.getDataLocation());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setParam(line.getParam());
            resultLine.setFormula(line.getFormula());
            resultLine.setRate(line.getRate());
            resultLine.setUnit(line.getParam().getUnit());
            resultLine.setIsBold(line.getIsBold());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setVal(value);
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(LocalDateTime.now());
            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines(resultLines);
    }

    private void copyInfoRows(SegResultHeader header) {
        List<SegResultLine> resultLines = new ArrayList<>();
        for (BalanceUnitLine line : header.getBalanceUnit().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.INFO)
                continue;

            SegResultLine resultLine = new SegResultLine();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setTreatmentType(line.getTreatmentType());
            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines(resultLines);
    }

    private void copyTranslates(BalanceUnitLine line, SegResultLine resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (BalanceUnitLineTranslate lineTranslate : line.getTranslates()) {
            SegResultLineTranslate resultLineTranslate = new SegResultLineTranslate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);

            resultLineTranslate.setName(lineTranslate.getName());
            resultLineTranslate.setShortName(lineTranslate.getShortName());
            if (resultLineTranslate == null && resultLine.getMeteringPoint()!=null)
                resultLineTranslate.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void copyNotes(SegResultHeader header) {
        List<SegResultNote> resultNotes = new ArrayList<>();
        for (SegNote note : header.getHeader().getNotes()) {
            SegResultNote resultNote = new SegResultNote();
            resultNote.setHeader(header);
            resultNote.setLineNum(note.getLineNum());
            resultNote.setNoteNum(note.getNoteNum());

            resultNote.setTranslates(Optional.ofNullable(resultNote.getTranslates()).orElse(new ArrayList<>()));
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void copyApps(SegResultHeader header) {
        List<SegResultApp> resultApps = new ArrayList<>();
        for (SegApp app : header.getHeader().getApps()) {
            SegResultApp resultApp = new SegResultApp();
            resultApp.setHeader(header);
            resultApp.setAppNum(app.getAppNum());

            resultApp.setTranslates(Optional.ofNullable(resultApp.getTranslates()).orElse(new ArrayList<>()));
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines(List<SegResultLine> lines) {
        segResultLineRepo.save(lines);
        segResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(SegResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateStatus(SegResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        segResultHeaderRepo.save(header);
        segResultHeaderRepo.flush();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    private Map<String, String> buildMsgParams(BalanceUnitLine line) {
        Map<String, String> msgParams = new HashMap<>();
        msgParams.put("line", line.getLineNum().toString());
        return msgParams;
    }
}
