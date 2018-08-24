package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.formula.CalcContext;
import calc.formula.expression.impl.AtTimeValueExpression;
import calc.formula.service.AtTimeValueService;
import calc.formula.service.MeteringReading;
import calc.formula.service.MeteringReadingService;
import calc.repo.calc.MeterHistoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("Duplicates")
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeteringReadingServiceImpl implements MeteringReadingService {
    private final MeterHistoryRepo meterHistoryRepo;
    private final AtTimeValueService atTimeValueService;

    @Override
    public List<MeteringReading> calc(MeteringPoint meteringPoint, BypassMode bypassMode, Parameter parameter, CalcContext context) {
        LocalDateTime startDate = context.getStartDate().atStartOfDay();
        LocalDateTime endDate = context.getEndDate().atStartOfDay().plusDays(1);

        List<MeterHistory> meterHistories = meterHistoryRepo.findAllByMeteringPointIdAndDate(meteringPoint.getId(), startDate, endDate);
        List<MeteringReading> lines = new ArrayList<>();

        if (meterHistories.size() == 0) {
            MeteringReading line = new MeteringReading();
            line.setParameter(parameter);
            line.setStartMeteringDate(startDate);
            line.setEndMeteringDate(endDate);
            line.setStartVal(getStartVal(meteringPoint, parameter, context));
            line.setEndVal(getEndVal(meteringPoint, parameter, context));
            lines.add(line);
            return Arrays.asList(line);
        }

        for (MeterHistory meterHistory : meterHistories) {
            LocalDateTime meterStartDate = Optional.ofNullable(meterHistory.getStartDate()).orElse(LocalDateTime.MIN);
            LocalDateTime meterEndDate = Optional.ofNullable(meterHistory.getEndDate()).orElse(LocalDateTime.MAX);

            MeteringReading line = new MeteringReading();
            line.setParameter(parameter);
            line.setMeteringPoint(meteringPoint);
            line.setMeter(meterHistory.getMeter());
            line.setMeterHistory(meterHistory);
            line.setMeterRate(meterHistory.getFactor());
            line.setBypassMode(bypassMode);

            if (bypassMode == null) {
                if (meterStartDate.isBefore(startDate)) {
                    line.setStartVal(getStartVal(meteringPoint, parameter, context));
                    line.setStartMeteringDate(startDate);
                }

                if (!meterStartDate.isBefore(startDate)) {
                    line.setStartVal(getMeterStartVal(meterHistory, parameter));
                    line.setStartMeteringDate(meterHistory.getStartDate());
                }

                if (meterEndDate.isAfter(endDate)) {
                    line.setEndVal(getEndVal(meteringPoint, parameter, context));
                    line.setEndMeteringDate(endDate);
                }

                if (!meterEndDate.isAfter(endDate)) {
                    line.setEndVal(getMeterEndVal(meterHistory, parameter));
                    line.setEndMeteringDate(meterHistory.getEndDate());
                }
            }

            if (bypassMode!=null) {
                LocalDateTime bypassStartDate = Optional.ofNullable(bypassMode.getStartDate()).orElse(LocalDateTime.MIN);
                LocalDateTime bypassEndDate = Optional.ofNullable(bypassMode.getEndDate()).orElse(LocalDateTime.MAX);

                if (meterStartDate.isBefore(startDate)) {
                    if (bypassStartDate.isBefore(startDate)) {
                        line.setStartVal(getStartVal(meteringPoint, parameter, context));
                        line.setStartMeteringDate(startDate);
                    }
                    else {
                        line.setStartVal(getBypassStartVal(bypassMode, parameter));
                        line.setStartMeteringDate(bypassMode.getStartDate());
                    }
                }

                if (meterEndDate.isAfter(endDate)) {
                    if (bypassEndDate.isAfter(endDate)) {
                        line.setEndVal(getEndVal(meteringPoint, parameter, context));
                        line.setEndMeteringDate(endDate);
                    }
                    else {
                        line.setEndVal(getBypassEndVal(bypassMode, parameter));
                        line.setEndMeteringDate(bypassMode.getEndDate());
                    }
                }

                if (!meterStartDate.isBefore(startDate)) {
                    if (bypassStartDate.isBefore(meterStartDate)) {
                        line.setStartVal(getMeterStartVal(meterHistory, parameter));
                        line.setStartMeteringDate(meterHistory.getStartDate());
                    }
                    else {
                        line.setStartVal(getBypassStartVal(bypassMode, parameter));
                        line.setStartMeteringDate(bypassMode.getStartDate());
                    }
                }

                if (!meterEndDate.isAfter(endDate)) {
                    if (meterEndDate.isBefore(bypassEndDate)) {
                        line.setEndVal(getMeterEndVal(meterHistory, parameter));
                        line.setEndMeteringDate(meterHistory.getEndDate());
                    }
                    else {
                        line.setEndVal(getBypassEndVal(bypassMode, parameter));
                        line.setEndMeteringDate(bypassMode.getEndDate());
                    }
                }
            }

            if (meterHistory.getUndercount()!=null && meterHistory.getUndercount().getParameter().equals(parameter)) {
                line.setUnderCount(meterHistory.getUndercount());
                line.setUnderCountVal(meterHistory.getUndercount().getVal());
            }

            if (line.getEndMeteringDate().isAfter(line.getStartMeteringDate()))
                lines.add(line);
        }

        return null;
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

    private Double getStartVal(MeteringPoint meteringPoint, Parameter param, CalcContext context) {
        return AtTimeValueExpression.builder()
            .meteringPointCode(meteringPoint.getCode())
            .parameterCode(param.getCode())
            .rate(1d)
            .per("start")
            .context(context)
            .service(atTimeValueService)
            .build()
            .doubleValue();
    }

    private Double getEndVal(MeteringPoint meteringPoint, Parameter param, CalcContext context) {
        return AtTimeValueExpression.builder()
            .meteringPointCode(meteringPoint.getCode())
            .parameterCode(param.getCode())
            .rate(1d)
            .per("end")
            .context(context)
            .service(atTimeValueService)
            .build()
            .doubleValue();
    }

}
