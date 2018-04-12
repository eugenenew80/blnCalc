package calc.formula.service.impl;

import calc.entity.Value;
import calc.formula.CalcContext;
import calc.entity.MeteringPoint;
import calc.entity.Parameter;
import calc.entity.PeriodTimeValue;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.MeteringPointRepo;
import calc.repo.ParameterRepo;
import calc.repo.PeriodTimeValueRepo;
import calc.repo.ValueRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PeriodTimeValueServiceImpl implements PeriodTimeValueService {
    private final PeriodTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;
    private final ValueRepo valueRepo;

    @Override
    public Double getValue(
        String meteringPointCode,
        String parameterCode,
        String src,
        String interval,
        Byte startHour,
        Byte endHour,
        CalcContext context) {

        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        Parameter parameter = parameterRepo.findByCodeAndParamType(parameterCode, "PT");

        if (meteringPoint.getMeteringPointTypeId()==2)
            return calcValue(context, meteringPoint);

        if (meteringPoint == null && parameter == null)
            return 0d;

        return ptValue(interval, startHour, endHour, context, meteringPoint, parameter);
    }

    private Double calcValue(CalcContext context, MeteringPoint meteringPoint) {
        Value value = valueRepo.findAllByMeteringPointIdAndStartDateAndEndDate(
            meteringPoint.getId(),
            context.getStartDate(),
            context.getEndDate()
        );

        return value!=null ? value.getVal() : 0d;
    }

    private Double ptValue(String interval, Byte startHour, Byte endHour, CalcContext context, MeteringPoint meteringPoint, Parameter parameter) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        if (interval.equals("c")) {
            startDate = context.getStartDate()
                .atStartOfDay();

            endDate = context.getEndDate()
                .atStartOfDay()
                .plusDays(1)
                .minusHours(1);
        }
        else if (interval.equals("m")) {
            startDate = context.getStartDate()
                .atStartOfDay()
                .truncatedTo(ChronoUnit.DAYS)
                .minusDays(context.getStartDate().getDayOfMonth()-1);

            endDate = context.getEndDate()
                .atStartOfDay()
                .plusDays(1)
                .minusHours(1);
        }
        else {
            startDate = context.getStartDate().atStartOfDay();
            endDate = context.getEndDate().atStartOfDay();
        }

        List<PeriodTimeValue> list = repo.findAllByMeteringPointIdAndParamIdAndMeteringDateBetween(
            meteringPoint.getId(),
            parameter.getId(),
            startDate,
            endDate
        );

        if (list.isEmpty())
            return 0d;

        return list.stream()
            .filter(t -> t.getMeteringDate().getHour()>=startHour && t.getMeteringDate().getHour()<=endHour)
            .map(t -> t.getVal())
            .reduce((t1, t2) -> t1 + t2)
            .orElse(0d);
    }
}

