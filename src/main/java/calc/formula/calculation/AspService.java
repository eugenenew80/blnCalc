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
import static java.util.stream.Collectors.groupingBy;

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
        logger.info(docCode + " calculation for header " + headerId + " started");
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
            //header.setDataType(DataTypeEnum.OPER);

            updateStatus(header, BatchStatusEnum.C);
            logger.info(docCode + " calculation for header " + header.getId() + " completed");
            return true;
        }
        catch (Exception e) {
            logger.error(docCode + " calculation for header " + header.getId() + " terminated with exception: " + e.getMessage());
            e.printStackTrace();

            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", buildMsgParams(e));
            updateStatus(header, BatchStatusEnum.E);
            return false;
        }
    }

    private void calcRows(AspResultHeader header, CalcContext context)  {
        Map<TreatmentTypeEnum, List<AspLine>> map = header.getHeader()
            .getLines()
            .stream()
            .filter(t ->
                       t.getTreatmentType() == INFO
                    || (t.getTreatmentType() == OUT   && t.getMeteringPoint() != null)
                    || (t.getTreatmentType() == EMPTY && t.getMeteringPoint() != null)
                    || (t.getTreatmentType() == IN    && t.getMeteringPoint() != null && t.getMeteringPoint().getPointType() == PointTypeEnum.VMP)
            )
            .collect(groupingBy(AspLine::getTreatmentType));

        for (TreatmentTypeEnum treatmentType : Arrays.asList(INFO, IN, EMPTY, OUT)) {
            logger.trace("calc row type: " + treatmentType);

            List<AspResultLine> results = new ArrayList<>();
            for (AspLine line : map.getOrDefault(treatmentType, new ArrayList<>())) {
                MeteringPoint meteringPoint = line.getMeteringPoint();
                Parameter param = line.getParam();
                Formula formula = line.getFormula();
                Map<String, String> msgParams = buildMsgParams(meteringPoint);

                AspResultLine result = new AspResultLine();
                results.add(result);
                copyTranslates(line, result);
                result.setHeader(header);
                result.setLineNum(line.getLineNum());
                result.setTreatmentType(line.getTreatmentType());
                result.setIsBold(line.getIsBold());
                result.setFormula(formula);
                result.setMeteringPoint(line.getMeteringPoint());
                result.setParam(line.getParam());
                if (line.getParam() != null)
                    result.setUnit(line.getParam().getUnit());

                if (line.getTreatmentType() == INFO || meteringPoint == null || param == null || meteringPoint.getPointType() == PointTypeEnum.PMP)
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
                            .processOrder(ProcessOrderEnum.CALC)
                            .build();

                    CalcResult calc = (formula != null)
                        ? calcService.calcValue(formula, context, property)
                        : calcService.calcValue(meteringPoint, param, context, property);

                    val = calc != null ? round(calc.getDoubleValue(),param) : null;
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
    }

    private void readRows(AspResultHeader header, CalcContext context) {
        logger.trace("reading metering data");
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void saveLines(List<AspResultLine> lines) {
        aspResultLineRepo.save(lines);
        aspResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    public void deleteMessages(AspResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void updateStatus(AspResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        aspResultHeaderRepo.save(header);
        aspResultHeaderRepo.flush();
    }
}