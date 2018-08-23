package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.impl.AtTimeValueExpression;
import calc.formula.service.AtTimeValueService;
import calc.formula.service.CalcService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class BalanceSubstMrService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstMrService.class);
    private final CalcService calcService;
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final ParameterRepo parameterRepo;
    private final UnitRepo unitRepo;
    private final AtTimeValueService atTimeValueService;
    private final BypassModeRepo bypassModeRepo;
    private final UndercountRepo undercountRepo;
    private final MeterHistoryRepo meterHistoryRepo;

    public void calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Metering reading for header " + header.getId() + " started");
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            header = balanceSubstResultHeaderRepo.findOne(header.getId());

            Map<String, Parameter> mapParams = new HashMap<>();
            mapParams.put("A-", parameterRepo.findByCode("A-"));
            mapParams.put("A+", parameterRepo.findByCode("A+"));
            mapParams.put("R-", parameterRepo.findByCode("R-"));
            mapParams.put("R+", parameterRepo.findByCode("R+"));

            Map<String, Unit> mapUnits = new HashMap<>();
            mapUnits.put("A-", unitRepo.findByCode("kW.h"));
            mapUnits.put("A+", unitRepo.findByCode("kW.h"));
            mapUnits.put("R-", unitRepo.findByCode("kVAR.h"));
            mapUnits.put("R+", unitRepo.findByCode("kVAR.h"));

            CalcContext context = CalcContext.builder()
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .docCode("ACT")
                .docId(header.getId())
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            LocalDateTime startDate = context.getStartDate().atStartOfDay();
            LocalDateTime endDate = context.getEndDate().atStartOfDay().plusDays(1);

            List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
            for (BalanceSubstMrLine mrLine : header.getHeader().getMrLines()) {
                List<BalanceSubstResultMrLine> lines = calcLines(mrLine.getMeteringPoint(), null, mapParams, context);
                for (BalanceSubstResultMrLine line : lines) {
                    line.setHeader(header);
                    line.setMeteringPoint(mrLine.getMeteringPoint());
                    line.setUnit(mapUnits.get(line.getParam().getCode()));
                    line.setSection(getSection(mrLine));
                    line.setIsIgnore(false);
                    line.setIsBypassSection(false);
                    line.setBypassMeteringPoint(null);

                    if (line.getStartVal() != null || line.getEndVal() !=null)
                        line.setDelta(Optional.ofNullable(line.getEndVal()).orElse(0d) - Optional.ofNullable(line.getStartVal()).orElse(0d));

                    if (line.getMeterRate()!=null && line.getDelta()!=null)
                        line.setVal(line.getDelta() * line.getMeterRate());
                }
                resultLines.addAll(lines);


                for (BypassMode bypassMode : bypassModeRepo.findAllByMeteringPointIdAndDate(mrLine.getMeteringPoint().getId(), startDate, endDate)) {
                    lines = calcLines(bypassMode.getBypassMeteringPoint(), bypassMode, mapParams, context);
                    for (BalanceSubstResultMrLine line : lines) {
                        line.setHeader(header);
                        line.setMeteringPoint(mrLine.getMeteringPoint());
                        line.setBypassMeteringPoint(bypassMode.getBypassMeteringPoint());
                        line.setUnit(mapUnits.get(line.getParam().getCode()));
                        line.setSection(getSection(mrLine));
                        line.setIsIgnore(false);
                        line.setIsBypassSection(bypassMode.getIsBusSection());

                        if (line.getStartVal() != null || line.getEndVal() !=null)
                            line.setDelta(Optional.ofNullable(line.getEndVal()).orElse(0d) - Optional.ofNullable(line.getStartVal()).orElse(0d));

                        if (line.getMeterRate()!=null && line.getDelta()!=null)
                            line.setVal(line.getDelta() * line.getMeterRate());
                    }
                    resultLines.addAll(lines);
                }
            }

            balanceSubstResultMrLineRepo.save(resultLines);
            updateStatus(header, BatchStatusEnum.C);
            logger.info("Metering reading for header " + header.getId() + " completed");
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMrLine> lines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultMrLineRepo.delete(lines.get(i));
        balanceSubstResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }

    private List<BalanceSubstResultMrLine> calcLines(MeteringPoint meteringPoint, BypassMode bypassMode, Map<String, Parameter> mapParams, CalcContext context) {
        LocalDateTime startDate = context.getStartDate().atStartOfDay();
        LocalDateTime endDate = context.getEndDate().atStartOfDay().plusDays(1);

        List<MeterHistory> meters = meterHistoryRepo.findAllByMeteringPointIdAndDate(meteringPoint.getId(), startDate, endDate);
        List<Parameter> parameters = getParameters(meters, mapParams);

        List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
        for (Parameter param : parameters) {
            if (meters.size() == 0) {
                BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();
                line.setParam(param);
                line.setStartMeteringDate(startDate);
                line.setEndMeteringDate(endDate);
                DoubleExpression start = getStartBalance(meteringPoint, param, context);
                line.setStartVal(start.doubleValue());
                DoubleExpression end = getEndBalance(meteringPoint, param, context);
                line.setEndVal(end.doubleValue());
                resultLines.add(line);
                continue;
            }

            for (MeterHistory meter : meters) {
                BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();
                line.setParam(param);
                line.setMeteringPoint(meteringPoint);
                line.setMeter(meter.getMeter());
                line.setMeterRate(meter.getFactor());

                if (bypassMode == null) {
                    if (meter.getStartDate() == null || meter.getStartDate().isBefore(startDate)) {
                        DoubleExpression start = getStartBalance(meteringPoint, param, context);
                        line.setStartVal(start.doubleValue());
                        line.setStartMeteringDate(startDate);
                    }
                    if (meter.getStartDate() != null && !meter.getStartDate().isBefore(startDate)) {
                        line.setStartVal(getNewVal(meter, param));
                        line.setStartMeteringDate(meter.getStartDate());
                    }
                    if (meter.getEndDate() == null || meter.getEndDate().isAfter(endDate)) {
                        DoubleExpression end = getEndBalance(meteringPoint, param, context);
                        line.setEndVal(end.doubleValue());
                        line.setEndMeteringDate(endDate);
                    }
                    if (meter.getEndDate() != null && !meter.getEndDate().isAfter(endDate)) {
                        line.setEndVal(getPrevVal(meter, param));
                        line.setEndMeteringDate(meter.getEndDate());
                    }
                }

                if (bypassMode!=null) {
                    if (meter.getStartDate() == null || meter.getStartDate().isBefore(startDate)) {
                        if (bypassMode.getStartDate()==null || bypassMode.getStartDate().isBefore(startDate)) {
                            DoubleExpression start = getStartBalance(meteringPoint, param, context);
                            line.setStartVal(start.doubleValue());
                            line.setStartMeteringDate(startDate);
                        }
                    }
                    if (meter.getStartDate() != null && !meter.getStartDate().isBefore(startDate)) {
                        line.setStartVal(getNewVal(meter, param));
                        line.setStartMeteringDate(meter.getStartDate());
                    }
                    if (meter.getEndDate() == null || meter.getEndDate().isAfter(endDate)) {
                        DoubleExpression end = getEndBalance(meteringPoint, param, context);
                        line.setEndVal(end.doubleValue());
                        line.setEndMeteringDate(endDate);
                    }
                    if (meter.getEndDate() != null && !meter.getEndDate().isAfter(endDate)) {
                        line.setEndVal(getPrevVal(meter, param));
                        line.setEndMeteringDate(meter.getEndDate());
                    }
                }

                if (meter.getUndercount()!=null && meter.getUndercount().getParameter().equals(param))
                    line.setUndercount(meter.getUndercount());

                resultLines.add(line);
            }
        }
        return resultLines;
    }

    private String getSection(BalanceSubstMrLine mrLine) {
        if (mrLine.getIsSection1()) return "1";
        if (mrLine.getIsSection2()) return "2";
        if (mrLine.getIsSection3()) return "3";
        if (mrLine.getIsSection4()) return "4";
        if (mrLine.getIsSection5()) return "5";
        return "";
    }

    private List<Parameter> getParameters(List<MeterHistory> meters, Map<String, Parameter> mapParams ) {
        if (meters==null || meters.size()==0)
            return mapParams.values().stream().collect(toList());

        List<Parameter> parameters = new ArrayList<>();
        if (meters.get(0).getMeter().getIsAm()) parameters.add(mapParams.get("A-"));
        if (meters.get(0).getMeter().getIsAp()) parameters.add(mapParams.get("A+"));
        if (meters.get(0).getMeter().getIsRm()) parameters.add(mapParams.get("R-"));
        if (meters.get(0).getMeter().getIsRp()) parameters.add(mapParams.get("R+"));
        return parameters;
    }

    private Double getPrevVal(MeterHistory meter, Parameter param) {
        if (param.getCode().equals("A+")) return meter.getApPrev();
        if (param.getCode().equals("A-")) return meter.getAmPrev();
        if (param.getCode().equals("R+")) return meter.getRpPrev();
        if (param.getCode().equals("R-")) return meter.getRmPrev();
        return null;
    }

    private Double getNewVal(MeterHistory meter, Parameter param) {
        if (param.getCode().equals("A+")) return meter.getApNew();
        if (param.getCode().equals("A-")) return meter.getAmNew();
        if (param.getCode().equals("R+")) return meter.getRpNew();
        if (param.getCode().equals("R-")) return meter.getRmNew();
        return null;
    }

    private DoubleExpression getEndBalance(MeteringPoint meteringPoint, Parameter param, CalcContext context) {
        return AtTimeValueExpression.builder()
            .meteringPointCode(meteringPoint.getCode())
            .parameterCode(param.getCode())
            .rate(1d)
            .per("end")
            .context(context)
            .service(atTimeValueService)
            .build();
    }

    private DoubleExpression getStartBalance(MeteringPoint meteringPoint, Parameter param, CalcContext context) {
        return AtTimeValueExpression.builder()
            .meteringPointCode(meteringPoint.getCode())
            .parameterCode(param.getCode())
            .rate(1d)
            .per("start")
            .context(context)
            .service(atTimeValueService)
            .build();
    }
}
