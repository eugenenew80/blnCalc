package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.asp.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.TreatmentTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.formula.service.MessageService;
import calc.formula.service.MeteringReading;
import calc.formula.service.MrService;
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
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
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
    private final ParameterRepo parameterRepo;
    private static final String docCode = "ASP1";
    private Map<String, Parameter> mapParams = null;
    private final CalcService calcService;

    @PostConstruct
    public void init() {
        mapParams = new HashMap<>();
        mapParams.put("A-", parameterRepo.findByCode("A-"));
        mapParams.put("A+", parameterRepo.findByCode("A+"));
        mapParams.put("R-", parameterRepo.findByCode("R-"));
        mapParams.put("R+", parameterRepo.findByCode("R+"));
    }

    public boolean calc(AspResultHeader header) {
        try {
            logger.info("Metering reading for header " + header.getId() + " started");
            header = aspResultHeaderRepo.findOne(header.getId());
            if (header.getStatus() == BatchStatusEnum.E)
                return false;

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .docId(header.getId())
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .isAsp(true)
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header);

            calcInfoRows(header);
            calcInRows(header, context);
            calcEmptyRows(header, context);
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
            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION");
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void calcOutRows(AspResultHeader header, CalcContext context) throws Exception {
        List<AspResultLine> resultLines = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.OUT)
                continue;;

            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();
            if (meteringPoint.getMeteringPointTypeId().equals(2l)) {
                CalcResult result = calcService.calcMeteringPoint(meteringPoint, param.getCode(), context);
                Double value = result!=null ? result.getDoubleValue() : null;
                if (param != null) {
                    double rounding =  Math.pow(10, Optional.ofNullable(param.getDigitsRounding()).orElse(0));
                    if (value != null) value = Math.round(value * rounding) / rounding;
                }

                AspResultLine resultLine = new AspResultLine();
                resultLine.setHeader(header);
                resultLine.setLineNum(line.getLineNum());
                resultLine.setMeteringPoint(line.getMeteringPoint());
                resultLine.setParam(line.getParam());
                resultLine.setUnit(line.getParam().getUnit());
                resultLine.setFormula(line.getFormula());
                resultLine.setTreatmentType(line.getTreatmentType());
                resultLine.setVal(value);

                addTranslates(line, resultLine);
                resultLines.add(resultLine);
            }
        }
        saveLines(resultLines);
    }

    private void calcEmptyRows(AspResultHeader header, CalcContext context) throws Exception {
        List<AspResultLine> resultLines = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.EMPTY)
                continue;

            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();
            if (meteringPoint.getMeteringPointTypeId().equals(2l)) {
                CalcResult result = calcService.calcMeteringPoint(meteringPoint, param.getCode(), context);
                Double value = result!=null ? result.getDoubleValue() : null;
                if (param != null) {
                    double rounding =  Math.pow(10, Optional.ofNullable(param.getDigitsRounding()).orElse(0));
                    if (value != null) value = Math.round(value * rounding) / rounding;
                }

                AspResultLine resultLine = new AspResultLine();
                resultLine.setHeader(header);
                resultLine.setLineNum(line.getLineNum());
                resultLine.setMeteringPoint(line.getMeteringPoint());
                resultLine.setParam(line.getParam());
                resultLine.setUnit(line.getParam().getUnit());
                resultLine.setFormula(line.getFormula());
                resultLine.setTreatmentType(line.getTreatmentType());
                resultLine.setVal(value);

                addTranslates(line, resultLine);
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

            List<MeteringReading> meteringReadings = mrService.calc(line.getMeteringPoint(), context)
                .stream()
                .filter(t -> t.getParam().equals(line.getParam()))
                .collect(Collectors.toList());

            if (meteringReadings.size() == 0) {
                AspResultLine resultLine = new AspResultLine();
                resultLine.setHeader(header);
                resultLine.setLineNum(line.getLineNum());
                resultLine.setMeteringPoint(line.getMeteringPoint());
                if (line.getParam() != null) {
                    resultLine.setParam(line.getParam());
                    resultLine.setUnit(line.getParam().getUnit());
                }
                resultLine.setFormula(line.getFormula());
                resultLine.setTreatmentType(line.getTreatmentType());
                addTranslates(line, resultLine);
                resultLines.add(resultLine);
            }

            for (MeteringReading t : meteringReadings) {
                AspResultLine resultLine = new AspResultLine();
                resultLine.setHeader(header);
                resultLine.setLineNum(line.getLineNum());
                resultLine.setMeteringPoint(line.getMeteringPoint());
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
                resultLine.setVal(t.getVal());
                resultLine.setBypassMeteringPoint(t.getBypassMeteringPoint());
                resultLine.setBypassMode(t.getBypassMode());
                resultLine.setUnderCountVal(t.getUnderCountVal());
                resultLine.setUndercount(t.getUnderCount());
                resultLine.setTreatmentType(line.getTreatmentType());
                addTranslates(line, resultLine);
                resultLines.add(resultLine);
            }
        }
        saveLines(resultLines);
    }

    private void calcInfoRows(AspResultHeader header) {
        List<AspResultLine> resultLines = new ArrayList<>();
        for (AspLine line : header.getHeader().getLines()) {
            if (line.getTreatmentType() != TreatmentTypeEnum.INFO)
                continue;;

            AspResultLine resultLine = new AspResultLine();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setTreatmentType(line.getTreatmentType());
            addTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines(resultLines);
    }

    private void addTranslates(AspLine line, AspResultLine resultLine) {
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
}
