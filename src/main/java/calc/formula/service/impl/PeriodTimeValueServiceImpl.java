package calc.formula.service.impl;

import calc.controller.rest.dto.Result;
import calc.entity.*;
import calc.formula.CalcContext;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.MeteringPointRepo;
import calc.repo.ParameterRepo;
import calc.repo.PeriodTimeValueRepo;
import calc.repo.SourceTypePriorityRepo;
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
    public List<Result> getValues(
        String meteringPointCode,
        String parameterCode,
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

        List<Result> list = context.getValues()
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

    @Override
    public List<SourceTypePriority> getSourceTypes(String meteringPointCode, CalcContext context) {
        MeteringPoint meteringPoint = meteringPointRepo
            .findByCode(meteringPointCode);

        if (meteringPoint == null)
            return Collections.emptyList();

        return sourceTypePriorityRepo.findAllByMeteringPointId(meteringPoint.getId());
    }
}