package calc.formula.service.impl;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.PeriodTimeValue;
import calc.entity.calc.SourceTypePriority;
import calc.formula.CalcResult;
import calc.formula.CalcContext;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.calc.MeteringPointRepo;
import calc.repo.calc.ParameterRepo;
import calc.repo.calc.PeriodTimeValueRepo;
import calc.repo.calc.SourceTypePriorityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PeriodTimeValueServiceImpl implements PeriodTimeValueService {
    private final PeriodTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final SourceTypePriorityRepo sourceTypePriorityRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public List<CalcResult> getValues(
        String meteringPointCode,
        String parameterCode,
        Byte startHour,
        Byte endHour,
        CalcContext context
    ) {
        MeteringPoint meteringPoint = meteringPointRepo
            .findByCode(meteringPointCode);

        Parameter parameter = parameterRepo
            .findByCode(parameterCode);

        if (meteringPoint == null || parameter == null)
            return Collections.emptyList();

        List<CalcResult> list = context.getValues()
            .stream()
            .filter(t -> t.getParamType().equals("PT"))
            .filter(t -> t.getMeteringPointId().equals(meteringPoint.getId()))
            .filter(t -> t.getParamId().equals(parameter.getId()))
            .collect(toList());

        if (!list.isEmpty())
            return list;

        return findValues(meteringPoint, parameter, context)
            .stream()
            .filter(t -> t.getMeteringDate().getHour()>=startHour && t.getMeteringDate().getHour()<=endHour)
            .map(PeriodTimeValue::toResult)
            .collect(toList());
    }

    private List<PeriodTimeValue> findValues(MeteringPoint meteringPoint, Parameter parameter, CalcContext context) {
        LocalDateTime startDate = context.getStartDate()
            .atStartOfDay();

        LocalDateTime endDate = context.getEndDate()
            .atStartOfDay()
            .plusDays(1)
            .minusHours(1);

        return repo.findAllByMeteringPointIdAndParamIdAndMeteringDateBetween(
            meteringPoint.getId(),
            parameter.getId(),
            startDate,
            endDate
        );
    }
}