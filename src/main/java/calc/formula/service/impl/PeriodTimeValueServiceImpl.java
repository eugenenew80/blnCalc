package calc.formula.service.impl;

import calc.formula.CalcContext;
import calc.entity.MeteringPoint;
import calc.entity.Parameter;
import calc.entity.PeriodTimeValue;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.MeteringPointRepo;
import calc.repo.ParameterRepo;
import calc.repo.PeriodTimeValueRepo;
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
    private final ParameterRepo parameterRepo;

    @Override
    public List<PeriodTimeValue> getValues(
        String meteringPointCode,
        String parameterCode,
        String src,
        Byte startHour,
        Byte endHour,
        CalcContext context
    ) {
        MeteringPoint meteringPoint = meteringPointRepo
            .findByCode(meteringPointCode);

        Parameter parameter = parameterRepo
            .findByCodeAndParamType(parameterCode, "PT");

        if (meteringPoint == null || parameter == null)
            return Collections.emptyList();

        List<PeriodTimeValue> list = context.getPtValues()
            .stream()
            .filter(t -> t.getMeteringPointId().equals(meteringPoint.getId()))
            .filter(t -> t.getParamId().equals(parameter.getId()))
            .collect(toList());

        if (!list.isEmpty())
            return list;

        return findValues(meteringPoint, parameter, context)
            .stream()
            .filter(t -> t.getMeteringDate().getHour()>=startHour && t.getMeteringDate().getHour()<=endHour)
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