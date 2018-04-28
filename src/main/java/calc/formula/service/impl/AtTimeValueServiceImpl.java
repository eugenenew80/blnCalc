package calc.formula.service.impl;

import calc.formula.CalcResult;
import calc.entity.calc.SourceTypePriority;
import calc.formula.CalcContext;
import calc.entity.calc.AtTimeValue;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.formula.service.AtTimeValueService;
import calc.repo.calc.AtTimeValueRepo;
import calc.repo.calc.MeteringPointRepo;
import calc.repo.calc.ParameterRepo;
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
public class AtTimeValueServiceImpl implements AtTimeValueService {
    private final AtTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final SourceTypePriorityRepo sourceTypePriorityRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public List<CalcResult> getValue(
        String meteringPointCode,
        String parameterCode,
        CalcContext context
    ) {
        MeteringPoint meteringPoint = meteringPointRepo
            .findByCode(meteringPointCode);

        Parameter parameter = parameterRepo
            .findByCodeAndParamType(parameterCode, "AT");

        if (meteringPoint == null || parameter == null)
            return Collections.emptyList();

        List<CalcResult> list = context.getValues()
            .stream()
            .filter(t -> t.getParamType().equals("AT"))
            .filter(t -> t.getMeteringPointId().equals(meteringPoint.getId()))
            .filter(t -> t.getParamId().equals(parameter.getId()))
            .collect(toList());

        if (!list.isEmpty())
            return list;

        return findValues(meteringPoint, parameter, context)
            .stream()
            .map(AtTimeValue::toResult)
            .collect(toList());
    }

    private List<AtTimeValue> findValues(MeteringPoint meteringPoint, Parameter parameter, CalcContext context) {
        LocalDateTime date = context.getEndDate()
            .atStartOfDay()
            .plusDays(1);

        return repo.findAllByMeteringPointIdAndParamIdAndMeteringDate(
            meteringPoint.getId(),
            parameter.getId(),
            date
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
