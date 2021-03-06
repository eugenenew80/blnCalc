package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.PointTypeEnum;
import calc.formula.CalcContext;
import calc.formula.expression.impl.AtTimeValueExpression;
import calc.formula.service.AtTimeValueService;
import calc.formula.service.MeteringReading;
import calc.formula.service.MrService;
import calc.repo.calc.BypassModeRepo;
import calc.repo.calc.MeterHistoryRepo;
import calc.repo.calc.UnderCountRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static calc.util.Util.round;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("Duplicates")
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MrServiceImpl implements MrService {
    private static final Logger logger = LoggerFactory.getLogger(MrServiceImpl.class);
    private final AtTimeValueService atTimeValueService;
    private final MeterHistoryRepo meterHistoryRepo;
    private final BypassModeRepo bypassModeRepo;
    private final UnderCountRepo underCountRepo;

    @Override
    public List<MeteringReading> calc(MeteringPoint meteringPoint, CalcContext context) {
        if (meteringPoint == null)
            return Collections.emptyList();

        if (meteringPoint.getPointType() == PointTypeEnum.VMP)
            return Collections.emptyList();

        LocalDateTime startDate = context.getHeader().getStartDate().atStartOfDay();
        LocalDateTime endDate = context.getHeader().getEndDate().atStartOfDay().plusDays(1);

        List<MeteringReading> resultLines = calcMeteringPoint(meteringPoint, null, context);
        for (BypassMode bypassMode : bypassModeRepo.findAllByMeteringPoint(meteringPoint.getId(), startDate, endDate)) {
            List<MeteringReading> bypassLines = calcMeteringPoint(bypassMode.getBypassMeteringPoint(), bypassMode, context);
            for (MeteringReading byPassLine : bypassLines) {
                byPassLine.setBypassMeteringPoint(bypassMode.getBypassMeteringPoint());
                byPassLine.setIsBypassSection(bypassMode.getIsBusSection());
            }
            resultLines.addAll(bypassLines);
        }

        for (MeteringReading line : resultLines) {
            if (line.getStartVal() != null || line.getEndVal() != null) {
                Double delta = ofNullable(line.getEndVal()).orElse(0d) - ofNullable(line.getStartVal()).orElse(0d);
                line.setDelta(round(delta, 6));
            }

            if (line.getMeterRate() != null && line.getDelta() != null) {
                Double val = round(line.getDelta() * line.getMeterRate(), 6);
                line.setVal(val);
            }
        }

        return resultLines;
    }

    private List<MeteringReading> calcMeteringPoint(MeteringPoint meteringPoint, BypassMode bypassMode, CalcContext context) {
        logger.trace("mp: " + meteringPoint.getCode());

        LocalDateTime startDate = context.getHeader().getStartDate().atStartOfDay();
        LocalDateTime endDate = context.getHeader().getEndDate().atStartOfDay().plusDays(1);

        List<Parameter> parameters = meteringPoint.getParameters()
            .stream()
            .map(t -> t.getParameter())
            .filter(t -> t.getId() <= 4l)
            .collect(toList());

        List<MeterHistory> meterHistories = meterHistoryRepo.findAllByMeteringPoint(meteringPoint.getId(), startDate, endDate);
        List<MeteringReading> resultLines = new ArrayList<>();
        for (Parameter param : parameters) {
            logger.trace("  param: " + param.getCode());

            if (meterHistories.size() == 0) {
                logger.trace("  meter history not found");
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
                logger.trace("  meter history id: " + meterHistory.getId());

                LocalDateTime meterStartDate = ofNullable(meterHistory.getStartDate()).orElse(LocalDateTime.MIN);
                LocalDateTime meterEndDate = ofNullable(meterHistory.getEndDate()).orElse(LocalDateTime.MAX);

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
                    LocalDateTime bypassStartDate = ofNullable(bypassMode.getStartDate()).orElse(LocalDateTime.MIN);
                    LocalDateTime bypassEndDate = ofNullable(bypassMode.getEndDate()).orElse(LocalDateTime.MAX);

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

                if (meterEndDate.isAfter(context.getHeader().getEndDate().atStartOfDay()) ) {
                    List<UnderCount> list = underCountRepo.findAllByMeteringPoint(meteringPoint.getCode(), line.getStartMeteringDate(), line.getEndMeteringDate());
                    for (UnderCount c : list) {
                        logger.trace("under count row: " + c.getId() + ", " + c.getMeteringPoint().getCode());
                    }


                    Double underCountVal = list
                        .stream()
                        .filter(t -> t.getParameter() != null)
                        .filter(t -> t.getParameter().equals(param))
                        .map(t -> t.getVal())
                        .filter(t -> t != null)
                        .reduce((t1, t2) -> ofNullable(t1).orElse(0d) + ofNullable(t2).orElse(0d))
                        .orElse(null);

                    line.setUnderCountVal(underCountVal);
                    logger.trace("  undercount val: " + underCountVal);
                }

                if (line.getEndMeteringDate().isAfter(line.getStartMeteringDate()))
                    resultLines.add(line);

                logger.trace("  start val: " + line.getStartVal());
                logger.trace("  end val: " + line.getEndVal());

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
        if (param.getCode().equals("A+")) return meter.getApNew();
        if (param.getCode().equals("A-")) return meter.getAmNew();
        if (param.getCode().equals("R+")) return meter.getRpNew();
        if (param.getCode().equals("R-")) return meter.getRmNew();
        return null;
    }

    private Double getMeterEndVal(MeterHistory meter, Parameter param) {
        if (param.getCode().equals("A+")) return meter.getApPrev();
        if (param.getCode().equals("A-")) return meter.getAmPrev();
        if (param.getCode().equals("R+")) return meter.getRpPrev();
        if (param.getCode().equals("R-")) return meter.getRmPrev();
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
}