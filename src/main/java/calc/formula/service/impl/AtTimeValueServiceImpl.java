package calc.formula.service.impl;

import calc.controller.rest.dto.Result;
import calc.entity.SourceTypePriority;
import calc.formula.CalcContext;
import calc.entity.AtTimeValue;
import calc.entity.MeteringPoint;
import calc.entity.Parameter;
import calc.formula.service.AtTimeValueService;
import calc.repo.AtTimeValueRepo;
import calc.repo.MeteringPointRepo;
import calc.repo.ParameterRepo;
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
public class AtTimeValueServiceImpl implements AtTimeValueService {
    private final AtTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final SourceTypePriorityRepo sourceTypePriorityRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public List<Result> getValue(
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

        List<Result> list = context.getValues()
            .stream()
            .filter(t -> t.getParamType().equals("AT"))
            .filter(t -> t.getMeteringPointId().equals(meteringPoint.getId()))
            .filter(t -> t.getParamId().equals(parameter.getId()))
            .collect(toList());

        if (!list.isEmpty())
            return list;

        return findValues(meteringPoint, parameter, context)
            .stream()
            .map( t-> {
                Result result = new Result();
                result.setInterval(null);
                result.setMeteringDate(t.getMeteringDate());
                result.setMeteringPointId(t.getMeteringPointId());
                result.setParamId(t.getParamId());
                result.setParamType("AT");
                result.setUnitId(t.getUnitId());
                result.setVal(t.getVal());
                result.setSourceType(t.getSourceType());
                return result;
            })
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
