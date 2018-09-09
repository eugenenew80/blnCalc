package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.TreatmentTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.formula.service.MeteringReading;
import calc.formula.service.MeteringReadingService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class AspService {
    private static final Logger logger = LoggerFactory.getLogger(AspService.class);
    private final AspResultHeaderRepo aspResultHeaderRepo;
    private final AspResultLineRepo aspResultLineRepo;
    private final MeteringReadingService meteringReadingService;
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

            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .docId(header.getId())
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .isAsp(true)
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            calcInfoRows(header);
            calcInRows(header, context);
            calcEmptyRows(header, context);
            calcOutRows(header, context);

            updateStatus(header, BatchStatusEnum.C);
            logger.info("Metering reading for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void calcOutRows(AspResultHeader header, CalcContext context) throws Exception {
        List<AspResultLine> resultLines;
        resultLines = new ArrayList<>();
        for (AspLine aspLine : header.getHeader().getLines()) {
            if (aspLine.getTreatmentType() != TreatmentTypeEnum.OUT)
                continue;;

            MeteringPoint meteringPoint = aspLine.getMeteringPoint();
            Parameter param = aspLine.getParam();
            if (meteringPoint.getMeteringPointTypeId().equals(2l)) {
                List<CalcResult> results = calcService.calcMeteringPoints(Arrays.asList(meteringPoint), param.getCode(), context);
                Double value = results.size() > 0 ? results.get(0).getDoubleValue() : null;

                AspResultLine line = new AspResultLine();
                line.setHeader(header);
                line.setLineNum(aspLine.getLineNum());
                line.setMeteringPoint(aspLine.getMeteringPoint());
                line.setParam(aspLine.getParam());
                line.setUnit(aspLine.getParam().getUnit());
                line.setFormula(aspLine.getFormula());
                line.setTreatmentType(aspLine.getTreatmentType());
                line.setVal(value);
                resultLines.add(line);
            }
            saveLines(resultLines);
        }
    }

    private void calcEmptyRows(AspResultHeader header, CalcContext context) throws Exception {
        List<AspResultLine> resultLines;
        resultLines = new ArrayList<>();
        for (AspLine aspLine : header.getHeader().getLines()) {
            if (aspLine.getTreatmentType() != TreatmentTypeEnum.EMPTY)
                continue;

            MeteringPoint meteringPoint = aspLine.getMeteringPoint();
            Parameter param = aspLine.getParam();
            if (meteringPoint.getMeteringPointTypeId().equals(2l)) {
                List<CalcResult> results = calcService.calcMeteringPoints(Arrays.asList(meteringPoint), param.getCode(), context);
                Double value = results.size() > 0 ? results.get(0).getDoubleValue() : null;

                AspResultLine line = new AspResultLine();
                line.setHeader(header);
                line.setLineNum(aspLine.getLineNum());
                line.setMeteringPoint(aspLine.getMeteringPoint());
                line.setParam(aspLine.getParam());
                line.setUnit(aspLine.getParam().getUnit());
                line.setFormula(aspLine.getFormula());
                line.setTreatmentType(aspLine.getTreatmentType());
                line.setVal(value);
                resultLines.add(line);
            }

            saveLines(resultLines);
        }
    }

    private void calcInRows(AspResultHeader header, CalcContext context) {
        List<AspResultLine> resultLines;
        resultLines = new ArrayList<>();
        for (AspLine aspLine : header.getHeader().getLines()) {
            if (aspLine.getTreatmentType() != TreatmentTypeEnum.IN)
                continue;

            List<MeteringReading> meteringReadings = meteringReadingService.calc(aspLine.getMeteringPoint(), context)
                .stream()
                .filter(t -> t.getParam().equals(aspLine.getParam()))
                .collect(Collectors.toList());

            for (MeteringReading t : meteringReadings) {
                AspResultLine line = new AspResultLine();
                line.setHeader(header);
                line.setLineNum(aspLine.getLineNum());
                line.setMeteringPoint(aspLine.getMeteringPoint());
                line.setParam(t.getParam());
                line.setUnit(t.getUnit());
                line.setMeter(t.getMeter());
                line.setMeterHistory(t.getMeterHistory());
                line.setFormula(aspLine.getFormula());
                line.setStartMeteringDate(t.getStartMeteringDate());
                line.setEndMeteringDate(t.getEndMeteringDate());
                line.setStartVal(t.getStartVal());
                line.setEndVal(t.getEndVal());
                line.setDelta(t.getDelta());
                line.setMeterRate(t.getMeterRate());
                line.setVal(t.getVal());
                line.setBypassMeteringPoint(t.getBypassMeteringPoint());
                line.setBypassMode(t.getBypassMode());
                line.setUnderCountVal(t.getUnderCountVal());
                line.setUndercount(t.getUnderCount());
                line.setTreatmentType(aspLine.getTreatmentType());
                resultLines.add(line);
            }
            saveLines(resultLines);
        }
    }

    private void calcInfoRows(AspResultHeader header) {
        List<AspResultLine> resultLines = new ArrayList<>();
        for (AspLine aspLine : header.getHeader().getLines()) {
            if (aspLine.getTreatmentType() != TreatmentTypeEnum.INFO)
                continue;;

            AspResultLine line = new AspResultLine();
            line.setHeader(header);
            line.setLineNum(aspLine.getLineNum());
            line.setTreatmentType(aspLine.getTreatmentType());
            resultLines.add(line);
            saveLines(resultLines);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLines(List<AspResultLine> lines) {
        aspResultLineRepo.save(lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(AspResultHeader header) {
        List<AspResultLine> lines = aspResultLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            aspResultLineRepo.delete(lines.get(i));
        aspResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(AspResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        aspResultHeaderRepo.save(header);
    }
}
