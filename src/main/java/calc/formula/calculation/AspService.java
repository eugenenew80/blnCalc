package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.asp.*;
import calc.entity.calc.enums.*;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextType;
import calc.formula.exception.CycleDetectionException;
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
import static calc.util.Util.round;

@SuppressWarnings({"ImplicitSubclassInspection", "Duplicates"})
@Service
@RequiredArgsConstructor
public class AspService {
    private static final Logger logger = LoggerFactory.getLogger(AspService.class);
    private final AspResultHeaderRepo aspResultHeaderRepo;
    private final AspResultLineRepo aspResultLineRepo;
    private final AspResultNoteRepo aspResultNoteRepo;
    private final AspResultAppRepo aspResultAppRepo;
    private final MessageService messageService;
    private final MrService mrService;
    private static final String docCode = "ASP1";
    private final CalcService calcService;

    public boolean calc(Long headerId) {
        logger.info("Metering reading for header " + headerId + " started");
        AspResultHeader header = aspResultHeaderRepo.findOne(headerId);
        if (header.getStatus() == BatchStatusEnum.E)
            return false;

        CalcContext context = CalcContext.builder()
            .lang(LangEnum.RU)
            .docCode(docCode)
            .headerId(header.getId())
            .periodType(header.getPeriodType())
            .startDate(header.getStartDate())
            .endDate(header.getEndDate())
            .orgId(header.getOrganization().getId())
            .contextType(ContextType.ASP)
            .values(new HashMap<>())
            .build();

        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header);

            copyInfoRows(header);
            calcInRows(header, context);
            calcIn2Rows(header, context);
            calcOutRows(header, context);
            copyNotes(header);
            copyApps(header);

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);
            header.setDataType(DataTypeEnum.OPER);

            updateStatus(header, BatchStatusEnum.C);
            logger.info("Metering reading for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            HashMap<String, String> msgParams = new HashMap<>();
            msgParams.putIfAbsent("err", e.getMessage());
            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", msgParams);

            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void calcOutRows(AspResultHeader header, CalcContext context)  {
        List<AspResultLine> resultLines = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.OUT && line.getTreatmentType() != TreatmentTypeEnum.EMPTY)
                continue;

            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();

            if (meteringPoint == null)
                continue;

            if (param == null)
                continue;

            Map<String, String> msgParams = buildMsgParams(meteringPoint);

            if (meteringPoint.getPointType() == PointTypeEnum.VMP) {
                Double value = null;
                try {
                    CalcResult result = calcService.calcMeteringPoint(meteringPoint, param, ParamTypeEnum.PT, context);
                    value = result != null ? result.getDoubleValue() : null;
                    value = round(value, param);
                }
                catch (CycleDetectionException e) {
                    messageService.addMessage(header, line.getLineNum(), docCode, "CYCLED_FORMULA", msgParams);
                    e.printStackTrace();
                }
                catch (Exception e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, line.getLineNum(), docCode, "ERROR_FORMULA", msgParams);
                    e.printStackTrace();
                }

                AspResultLine resultLine = new AspResultLine();
                resultLine.setHeader(header);
                resultLine.setLineNum(line.getLineNum());
                resultLine.setMeteringPoint(line.getMeteringPoint());
                resultLine.setParam(line.getParam());
                resultLine.setUnit(line.getParam().getUnit());
                resultLine.setFormula(line.getFormula());
                resultLine.setTreatmentType(line.getTreatmentType());
                resultLine.setIsBold(line.getIsBold());
                resultLine.setVal(value);

                copyTranslates(line, resultLine);
                resultLines.add(resultLine);
            }
        }
        saveLines(resultLines);
    }

    private void calcInRows(AspResultHeader header, CalcContext context) {
        List<AspResultLine> resultLines = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.IN)
                continue;

            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();

            if (param == null)
                continue;

            if (param.getCode().equals("AB"))
                continue;

            List<MeteringReading> meteringReadings = mrService.calc(meteringPoint, context)
                .stream()
                .filter(t -> t.getParam().equals(param))
                .collect(Collectors.toList());

            if (meteringReadings.size() == 0) {
                AspResultLine resultLine = new AspResultLine();
                resultLine.setHeader(header);
                resultLine.setLineNum(line.getLineNum());
                resultLine.setMeteringPoint(meteringPoint);
                if (param != null) {
                    resultLine.setParam(param);
                    resultLine.setUnit(param.getUnit());
                }
                resultLine.setFormula(line.getFormula());
                resultLine.setTreatmentType(line.getTreatmentType());
                copyTranslates(line, resultLine);
                resultLines.add(resultLine);
            }

            for (MeteringReading t : meteringReadings) {
                Double val = round(t.getVal(), t.getParam());

                AspResultLine resultLine = new AspResultLine();
                resultLine.setHeader(header);
                resultLine.setLineNum(line.getLineNum());
                resultLine.setMeteringPoint(meteringPoint);
                resultLine.setParam(t.getParam());
                resultLine.setUnit(t.getUnit());
                resultLine.setMeter(t.getMeter());
                resultLine.setMeterHistory(t.getMeterHistory());
                resultLine.setFormula(line.getFormula());
                resultLine.setStartMeteringDate(t.getStartMeteringDate());
                resultLine.setEndMeteringDate(t.getEndMeteringDate());
                resultLine.setStartVal(t.getStartVal());
                resultLine.setEndVal(t.getEndVal());
                resultLine.setDelta(t.getDelta());
                resultLine.setMeterRate(t.getMeterRate());
                resultLine.setVal(val);
                resultLine.setBypassMeteringPoint(t.getBypassMeteringPoint());
                resultLine.setBypassMode(t.getBypassMode());
                resultLine.setUnderCountVal(t.getUnderCountVal());
                resultLine.setUndercount(t.getUnderCount());
                resultLine.setTreatmentType(line.getTreatmentType());
                resultLine.setIsBold(line.getIsBold());

                copyTranslates(line, resultLine);
                resultLines.add(resultLine);
            }
        }
        saveLines(resultLines);
    }

    private void calcIn2Rows(AspResultHeader header, CalcContext context) {
        List<AspResultLine> resultLines = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.IN)
                continue;

            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();

            if (param == null)
                continue;

            if (!param.getCode().equals("AB"))
                continue;

            Map<String, String> msgParams = buildMsgParams(meteringPoint);

            AspResultLine resultLine = new AspResultLine();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(meteringPoint);
            if (param != null) {
                resultLine.setParam(param);
                resultLine.setUnit(param.getUnit());
            }
            resultLine.setFormula(line.getFormula());
            resultLine.setTreatmentType(line.getTreatmentType());

            Double value = null;
            try {
                CalcResult result = calcService.calcMeteringPoint(meteringPoint, param, ParamTypeEnum.PT, context);
                value = result != null ? result.getDoubleValue() : null;
                value = round(value, param);
            }
            catch (CycleDetectionException e) {
                messageService.addMessage(header, line.getLineNum(), docCode, "CYCLED_FORMULA", msgParams);
                e.printStackTrace();
            }
            catch (Exception e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getLineNum(), docCode, "ERROR_FORMULA", msgParams);
                e.printStackTrace();
            }

            resultLine.setVal(value);
            resultLine.setIsBold(line.getIsBold());

            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }

        saveLines(resultLines);
    }

    private void copyInfoRows(AspResultHeader header) {
        List<AspResultLine> resultLines = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.INFO)
                continue;

            AspResultLine resultLine = new AspResultLine();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setTreatmentType(line.getTreatmentType());
            resultLine.setIsBold(line.getIsBold());

            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines(resultLines);
    }

    private void copyTranslates(AspLine line, AspResultLine resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (AspLineTranslate lineTranslate : line.getTranslates()) {
            AspResultLineTranslate resultLineTranslate = new AspResultLineTranslate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);

            resultLineTranslate.setName(lineTranslate.getName());
            if (resultLineTranslate == null && resultLine.getMeteringPoint()!=null)
                resultLineTranslate.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void copyNotes(AspResultHeader header) {
        List<AspResultNote> resultNotes = new ArrayList<>();
        for (AspNote note : header.getHeader().getNotes()) {
            AspResultNote resultNote = new AspResultNote();
            resultNote.setHeader(header);
            resultNote.setLineNum(note.getLineNum());
            resultNote.setNoteNum(note.getNoteNum());

            resultNote.setTranslates(Optional.ofNullable(resultNote.getTranslates()).orElse(new ArrayList<>()));
            for (AspNoteTranslate noteTranslate : note.getTranslates()) {
                AspResultNoteTranslate resultNoteTranslate = new AspResultNoteTranslate();
                resultNoteTranslate.setNote(resultNote);
                resultNoteTranslate.setLang(noteTranslate.getLang());
                resultNoteTranslate.setNoteText(noteTranslate.getNoteText());
                resultNote.getTranslates().add(resultNoteTranslate);
            }
            resultNotes.add(resultNote);
        }
        aspResultNoteRepo.save(resultNotes);
        aspResultNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void copyApps(AspResultHeader header) {
        List<AspResultApp> resultApps = new ArrayList<>();
        for (AspApp app : header.getHeader().getApps()) {
            AspResultApp resultApp = new AspResultApp();
            resultApp.setHeader(header);
            resultApp.setAppNum(app.getAppNum());

            resultApp.setTranslates(Optional.ofNullable(resultApp.getTranslates()).orElse(new ArrayList<>()));
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
        for (int i=0; i<lines.size(); i++)
            aspResultLineRepo.delete(lines.get(i));
        aspResultLineRepo.flush();

        List<AspResultNote> notes = aspResultNoteRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<notes.size(); i++)
            aspResultNoteRepo.delete(notes.get(i));
        aspResultNoteRepo.flush();

        List<AspResultApp> apps = aspResultAppRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<apps.size(); i++)
            aspResultAppRepo.delete(apps.get(i));
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

    private Map<String, String> buildMsgParams(MeteringPoint mp) {
        Map<String, String> msgParams = new HashMap<>();
        if (mp != null) msgParams.put("point", mp.getCode());
        return msgParams;
    }
}
