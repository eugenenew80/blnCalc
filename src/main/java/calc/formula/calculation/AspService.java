package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.asp.*;
import calc.entity.calc.enums.*;
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
import java.util.stream.Collectors;
import static calc.entity.calc.enums.TreatmentTypeEnum.*;
import static calc.util.Util.*;
import static java.util.Optional.*;

@SuppressWarnings({"ImplicitSubclassInspection"})
@Service
@RequiredArgsConstructor
public class AspService {
    private static final Logger logger = LoggerFactory.getLogger(AspService.class);
    private static final String docCode = "ASP1";
    private final AspResultHeaderRepo aspResultHeaderRepo;
    private final AspResultLineRepo aspResultLineRepo;
    private final AspResultNoteRepo aspResultNoteRepo;
    private final AspResultAppRepo aspResultAppRepo;
    private final MessageService messageService;
    private final MrService mrService;
    private final CalcService calcService;

    public boolean calc(Long headerId) {
        logger.info("ASP calculation for header " + headerId + " started");
        AspResultHeader header = aspResultHeaderRepo.findOne(headerId);
        if (header.getStatus() != BatchStatusEnum.W)
            return false;

        if (header.getDataType() == null)
            header.setDataType(header.getPeriodType() == PeriodTypeEnum.M ? DataTypeEnum.FINAL : DataTypeEnum.OPER);

        CalcContext context = CalcContext.builder()
            .lang(LangEnum.RU)
            .header(header)
            .defContextType(ContextTypeEnum.ASP)
            .build();

        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header);

            readRows(header, context);
            calcRows(header, context);
            copyNotes(header);
            copyApps(header);

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);
            header.setDataType(DataTypeEnum.OPER);

            updateStatus(header, BatchStatusEnum.C);
            logger.info("ASP calculation for header " + header.getId() + " completed");
            return true;
        }
        catch (Exception e) {
            logger.error("ASP calculation for header " + header.getId() + " terminated with exception: " + e.getMessage());
            e.printStackTrace();

            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", buildMsgParams(e));
            updateStatus(header, BatchStatusEnum.E);
            return false;
        }
    }

    private void calcRows(AspResultHeader header, CalcContext context)  {
        List<AspResultLine> results = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();
            Formula formula = line.getFormula();
            Map<String, String> msgParams = buildMsgParams(meteringPoint);

            if (meteringPoint == null || param == null)
                continue;

            if (!(line.getTreatmentType() == INFO || line.getTreatmentType() == OUT || line.getTreatmentType() == EMPTY || meteringPoint.getPointType() == PointTypeEnum.VMP))
                continue;

            AspResultLine result = new AspResultLine();
            results.add(result);
            copyTranslates(line, result);

            result.setHeader(header);
            result.setLineNum(line.getLineNum());
            result.setMeteringPoint(line.getMeteringPoint());
            result.setParam(line.getParam());
            result.setUnit(line.getParam().getUnit());
            result.setFormula(formula);
            result.setTreatmentType(line.getTreatmentType());
            result.setIsBold(line.getIsBold());

            if (line.getTreatmentType() == INFO || meteringPoint.getPointType() != PointTypeEnum.VMP)
                continue;

            Double val = null;
            try {
                CalcProperty property;
                if (line.getTreatmentType() == IN)
                    property = CalcProperty.builder()
                        .contextType(ContextTypeEnum.DEFAULT)
                        .build();
                else
                    property = CalcProperty.builder()
                        .contextType(context.getDefContextType())
                        .build();

                CalcResult calc;
                if (formula != null)
                    calc = calcService.calcValue(formula, context, property);
                else
                    calc = calcService.calcValue(meteringPoint, param, context, property);

                val = calc != null ? calc.getDoubleValue() : null;
                val = round(val, param);
            }
            catch (CalcServiceException e) {
                e.printStackTrace();
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getLineNum(), docCode, e.getErrCode(), msgParams);
            }
            result.setVal(val);
        }
        saveLines(results);
    }

    private void readRows(AspResultHeader header, CalcContext context) {
        List<AspResultLine> results = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();
            Map<String, String> msgParams = buildMsgParams(meteringPoint);

            if (meteringPoint == null || param == null)
                continue;

            if (!(line.getTreatmentType() == IN && meteringPoint.getPointType() == PointTypeEnum.PMP))
                continue;

            List<MeteringReading> meteringReadings;
            try {
                meteringReadings = mrService.calc(meteringPoint, context)
                    .stream()
                    .filter(t -> t.getParam().equals(param))
                    .collect(Collectors.toList());
            }
            catch (CalcServiceException e) {
                e.printStackTrace();
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getLineNum(), docCode, e.getErrCode(), msgParams);
                continue;
            }

            for (MeteringReading t : meteringReadings) {
                AspResultLine result = new AspResultLine();
                result.setHeader(header);
                result.setLineNum(line.getLineNum());
                result.setMeteringPoint(meteringPoint);
                result.setParam(t.getParam());
                result.setUnit(t.getUnit());
                result.setMeter(t.getMeter());
                result.setMeterHistory(t.getMeterHistory());
                result.setFormula(line.getFormula());
                result.setStartMeteringDate(t.getStartMeteringDate());
                result.setEndMeteringDate(t.getEndMeteringDate());
                result.setStartVal(t.getStartVal());
                result.setEndVal(t.getEndVal());
                result.setDelta(t.getDelta());
                result.setMeterRate(t.getMeterRate());
                result.setVal(round(t.getVal(), t.getParam()));
                result.setBypassMeteringPoint(t.getBypassMeteringPoint());
                result.setBypassMode(t.getBypassMode());
                result.setUnderCountVal(t.getUnderCountVal());
                result.setUndercount(t.getUnderCount());
                result.setTreatmentType(line.getTreatmentType());
                result.setIsBold(line.getIsBold());
                copyTranslates(line, result);
                results.add(result);
            }
        }
        saveLines(results);
    }

    private void copyTranslates(AspLine line, AspResultLine resultLine) {
        resultLine.setTranslates(ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (AspLineTranslate ltl : line.getTranslates()) {
            AspResultLineTranslate rtl = new AspResultLineTranslate();
            rtl.setLang(ltl.getLang());
            rtl.setLine(resultLine);
            rtl.setName(ltl.getName());

            if (rtl.getName() == null && resultLine.getMeteringPoint()!=null)
                rtl.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(rtl);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void copyNotes(AspResultHeader header) {
        List<AspResultNote> results = new ArrayList<>();
        for (AspNote note : header.getHeader().getNotes()) {
            AspResultNote result = new AspResultNote();
            result.setHeader(header);
            result.setLineNum(note.getLineNum());
            result.setNoteNum(note.getNoteNum());

            result.setTranslates(ofNullable(result.getTranslates()).orElse(new ArrayList<>()));
            for (AspNoteTranslate noteTranslate : note.getTranslates()) {
                AspResultNoteTranslate resultTranslate = new AspResultNoteTranslate();
                resultTranslate.setNote(result);
                resultTranslate.setLang(noteTranslate.getLang());
                resultTranslate.setNoteText(noteTranslate.getNoteText());
                result.getTranslates().add(resultTranslate);
            }
            results.add(result);
        }
        aspResultNoteRepo.save(results);
        aspResultNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void copyApps(AspResultHeader header) {
        List<AspResultApp> resultApps = new ArrayList<>();
        for (AspApp app : header.getHeader().getApps()) {
            AspResultApp resultApp = new AspResultApp();
            resultApp.setHeader(header);
            resultApp.setAppNum(app.getAppNum());

            resultApp.setTranslates(ofNullable(resultApp.getTranslates()).orElse(new ArrayList<>()));
            for (AspAppTranslate appTranslate : app.getTranslates()) {
                AspResultAppTranslate resultAppTranslate = new AspResultAppTranslate();
                resultAppTranslate.setApp(resultApp);
                resultAppTranslate.setLang(appTranslate.getLang());
                resultAppTranslate.setAppName(appTranslate.getAppName());
                resultApp.getTranslates().add(resultAppTranslate);
            }
            resultApps.add(resultApp);
        }
        aspResultAppRepo.save(resultApps);
        aspResultAppRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines(List<AspResultLine> lines) {
        aspResultLineRepo.save(lines);
        aspResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void deleteLines(AspResultHeader header) {
        List<AspResultLine> lines = aspResultLineRepo.findAllByHeaderId(header.getId());
        aspResultLineRepo.delete(lines);
        aspResultLineRepo.flush();

        List<AspResultNote> notes = aspResultNoteRepo.findAllByHeaderId(header.getId());
        aspResultNoteRepo.delete(notes);
        aspResultNoteRepo.flush();

        List<AspResultApp> apps = aspResultAppRepo.findAllByHeaderId(header.getId());
        aspResultAppRepo.delete(apps);
        aspResultAppRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(AspResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateStatus(AspResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        aspResultHeaderRepo.save(header);
        aspResultHeaderRepo.flush();
    }
}
