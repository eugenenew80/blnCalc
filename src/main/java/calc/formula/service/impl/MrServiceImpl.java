package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.formula.CalcContext;
import calc.formula.expression.impl.AtTimeValueExpression;
import calc.formula.service.AtTimeValueService;
import calc.formula.service.MeteringReading;
import calc.formula.service.MrService;
import calc.repo.calc.BypassModeRepo;
import calc.repo.calc.MeterHistoryRepo;
import calc.repo.calc.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("Duplicates")
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MrServiceImpl implements MrService {
    private final ParameterRepo parameterRepo;
    private final AtTimeValueService atTimeValueService;
    private final MeterHistoryRepo meterHistoryRepo;
    private final BypassModeRepo bypassModeRepo;
    private Map<String, Parameter> mapParams = null;

    @PostConstruct
    public void init() {
        mapParams = new HashMap<>();
        mapParams.put("A-", parameterRepo.findByCode("A-"));
        mapParams.put("A+", parameterRepo.findByCode("A+"));
        mapParams.put("R-", parameterRepo.findByCode("R-"));
        mapParams.put("R+", parameterRepo.findByCode("R+"));
    }

    @Override
    public List<MeteringReading> calc(MeteringPoint meteringPoint, CalcContext context) {
        LocalDateTime startDate = context.getStartDate().atStartOfDay();
        LocalDateTime endDate = context.getEndDate().atStartOfDay().plusDays(1);

        List<MeteringReading> resultLines = calcMeteringPoint(meteringPoint, null, context);
        for (BypassMode bypassMode : bypassModeRepo.findAllByMeteringPointIdAndDate(meteringPoint.getId(), startDate, endDate)) {
            List<MeteringReading> bypassLines = calcMeteringPoint(bypassMode.getBypassMeteringPoint(), bypassMode, context);
            for (MeteringReading byPassLine : bypassLines) {
                byPassLine.setBypassMeteringPoint(bypassMode.getBypassMeteringPoint());
                byPassLine.setIsBypassSection(bypassMode.getIsBusSection());
            }
            resultLines.addAll(bypassLines);
        }

        for (MeteringReading line : resultLines) {
            if (line.getStartVal() != null || line.getEndVal() != null) {
                Double delta = Optional.ofNullable(line.getEndVal()).orElse(0d) - Optional.ofNullable(line.getStartVal()).orElse(0d);
                line.setDelta(Math.round(delta * 1000000d) / 1000000d);
            }

            if (line.getMeterRate() != null && line.getDelta() != null) {
                Double val = line.getDelta() * line.getMeterRate();
                line.setVal( Math.round(val) * 1d);
            }
        }

        return resultLines;
    }

    private List<MeteringReading> calcMeteringPoint(MeteringPoint meteringPoint, BypassMode bypassMode, CalcContext context) {
        LocalDateTime startDate = context.getStartDate().atStartOfDay();
        LocalDateTime endDate = context.getEndDate().atStartOfDay().plusDays(1);

        List<MeterHistory> meterHistories = meterHistoryRepo.findAllByMeteringPointIdAndDate(meteringPoint.getId(), startDate, endDate);
        List<Parameter> parameters = getParameters(meterHistories);

        List<MeteringReading> resultLines = new ArrayList<>();
        for (Parameter param : parameters) {
            if (meterHistories.size() == 0) {
                MeteringReading line = new MeteringReading();
                line.setParam(param);
                line.setUnit(param.getUnit());
                line.setStartMeteringDate(startDate);
                line.setEndMeteringDate(endDate);
                line.setStartVal(getAtTimeValue(meteringPoint, param, "start", context));
                line.setEndVal(getAtTimeValue(meteringPoint, param, "end", context));
                resultLines.add(line);
                continue;
            }

            for (MeterHistory meterHistory : meterHistories) {
                LocalDateTime meterStartDate = Optional.ofNullable(meterHistory.getStartDate()).orElse(LocalDateTime.MIN);
                LocalDateTime meterEndDate = Optional.ofNullable(meterHistory.getEndDate()).orElse(LocalDateTime.MAX);

                MeteringReading line = new MeteringReading();
                line.setParam(param);
                line.setUnit(param.getUnit());
                line.setMeteringPoint(meteringPoint);
                line.setMeter(meterHistory.getMeter());
                line.setMeterHistory(meterHistory);
                line.setMeterRate(meterHistory.getFactor());
                line.setBypassMode(bypassMode);

                if (bypassMode == null) {
                    if (meterStartDate.isBefore(startDate)) {
                        line.setStartVal(getAtTimeValue(meteringPoint, param, "start", context));
                        line.setStartMeteringDate(startDate);
                    }

                    if (!meterStartDate.isBefore(startDate)) {
                        line.setStartVal(getMeterStartVal(meterHistory, param));
                        line.setStartMeteringDate(meterHistory.getStartDate());
                    }

                    if (meterEndDate.isAfter(endDate)) {
                        line.setEndVal(getAtTimeValue(meteringPoint, param, "end", context));
                        line.setEndMeteringDate(endDate);
                    }

                    if (!meterEndDate.isAfter(endDate)) {
                        line.setEndVal(getMeterEndVal(meterHistory, param));
                        line.setEndMeteringDate(meterHistory.getEndDate());
                    }
                }

                if (bypassMode!=null) {
                    LocalDateTime bypassStartDate = Optional.ofNullable(bypassMode.getStartDate()).orElse(LocalDateTime.MIN);
                    LocalDateTime bypassEndDate = Optional.ofNullable(bypassMode.getEndDate()).orElse(LocalDateTime.MAX);

                    if (meterStartDate.isBefore(startDate)) {
                        if (bypassStartDate.isBefore(startDate)) {
                            line.setStartVal(getAtTimeValue(meteringPoint, param, "start", context));
                            line.setStartMeteringDate(startDate);
                        }
                        else {
                            line.setStartVal(getBypassStartVal(bypassMode, param));
                            line.setStartMeteringDate(bypassMode.getStartDate());
                        }
                    }

                    if (meterEndDate.isAfter(endDate)) {
                        if (bypassEndDate.isAfter(endDate)) {
                            line.setEndVal(getAtTimeValue(meteringPoint, param, "end", context));
                            line.setEndMeteringDate(endDate);
                        }
                        else {
                            line.setEndVal(getBypassEndVal(bypassMode, param));
                            line.setEndMeteringDate(bypassMode.getEndDate());
                        }
                    }

                    if (!meterStartDate.isBefore(startDate)) {
                        if (bypassStartDate.isBefore(meterStartDate)) {
                            line.setStartVal(getMeterStartVal(meterHistory, param));
                            line.setStartMeteringDate(meterHistory.getStartDate());
                        }
                        else {
                            line.setStartVal(getBypassStartVal(bypassMode, param));
                            line.setStartMeteringDate(bypassMode.getStartDate());
                        }
                    }

                    if (!meterEndDate.isAfter(endDate)) {
                        if (meterEndDate.isBefore(bypassEndDate)) {
                            line.setEndVal(getMeterEndVal(meterHistory, param));
                            line.setEndMeteringDate(meterHistory.getEndDate());
                        }
                        else {
                            line.setEndVal(getBypassEndVal(bypassMode, param));
                            line.setEndMeteringDate(bypassMode.getEndDate());
                        }
                    }
                }

                if (meterHistory.getUndercount()!=null && meterHistory.getUndercount().getParameter().equals(param)) {
                    line.setUnderCount(meterHistory.getUndercount());
                    line.setUnderCountVal(meterHistory.getUndercount().getVal());
                }

                if (line.getEndMeteringDate().isAfter(line.getStartMeteringDate()))
                    resultLines.add(line);
            }
        }
        return resultLines;
    }

    private Double getBypassStartVal(BypassMode bypassMode, Parameter param) {
        return bypassMode.getValues()
            .stream()
            .filter(v -> v.getParameter().equals(param))
            .map(v -> v.getStartValue())
            .findFirst()
            .orElse(null);
    }

    private Double getBypassEndVal(BypassMode bypassMode, Parameter param) {
        return bypassMode.getValues()
            .stream()
            .filter(v -> v.getParameter().equals(param))
            .map(v -> v.getEndValue())
            .findFirst()
            .orElse(null);
    }

    private Double getMeterStartVal(MeterHistory meter, Parameter param) {
        if (param.getCode().equals("A+")) return meter.getApPrev();
        if (param.getCode().equals("A-")) return meter.getAmPrev();
        if (param.getCode().equals("R+")) return meter.getRpPrev();
        if (param.getCode().equals("R-")) return meter.getRmPrev();
        return null;
    }

    private Double getMeterEndVal(MeterHistory meter, Parameter param) {
        if (param.getCode().equals("A+")) return meter.getApNew();
        if (param.getCode().equals("A-")) return meter.getAmNew();
        if (param.getCode().equals("R+")) return meter.getRpNew();
        if (param.getCode().equals("R-")) return meter.getRmNew();
        return null;
    }

    private Double getAtTimeValue(MeteringPoint meteringPoint, Parameter param, String per, CalcContext context) {
        return AtTimeValueExpression.builder()
            .meteringPointCode(meteringPoint.getCode())
            .parameterCode(param.getCode())
            .rate(1d)
            .per(per)
            .context(context)
            .service(atTimeValueService)
            .build()
            .doubleValue();
    }

    private List<Parameter> getParameters(List<MeterHistory> meters) {
        if (meters==null || meters.size()==0 || meters.get(0).getMeter() == null)
            return mapParams.values().stream().collect(toList());

        List<Parameter> parameters = new ArrayList<>();
        if (meters.get(0).getMeter().getIsAm()) parameters.add(mapParams.get("A-"));
        if (meters.get(0).getMeter().getIsAp()) parameters.add(mapParams.get("A+"));
        if (meters.get(0).getMeter().getIsRm()) parameters.add(mapParams.get("R-"));
        if (meters.get(0).getMeter().getIsRp()) parameters.add(mapParams.get("R+"));
        return parameters;
    }
}
