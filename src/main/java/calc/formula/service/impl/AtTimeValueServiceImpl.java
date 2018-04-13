package calc.formula.service.impl;

import calc.formula.CalcContext;
import calc.entity.AtTimeValue;
import calc.entity.MeteringPoint;
import calc.entity.Parameter;
import calc.formula.service.AtTimeValueService;
import calc.repo.AtTimeValueRepo;
import calc.repo.MeteringPointRepo;
import calc.repo.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AtTimeValueServiceImpl implements AtTimeValueService {
    private final AtTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public Double getValue(
        String meteringPointCode,
        String parameterCode,
        String src,
        String per,
        CalcContext context) {

        MeteringPoint meteringPoint = meteringPointRepo
            .findByCode(meteringPointCode);

        Parameter parameter = parameterRepo
            .findByCodeAndParamType(parameterCode, "AT");

        if (meteringPoint == null && parameter == null)
            return 0d;

        LocalDateTime date;
        if (per.equals("s"))
            date = context.getStartDate().atStartOfDay();
        else if (per.equals("e"))
            date = context.getEndDate()
                .atStartOfDay()
                .plusDays(1);
        else
            date = LocalDate.now().atStartOfDay();

        List<AtTimeValue> list = repo.findAllByMeteringPointIdAndParamIdAndMeteringDate(
            meteringPoint.getId(),
            parameter.getId(),
            date
        );

        if (list.isEmpty())
            return 0d;

        return list.stream()
            .map(t -> t.getVal())
            .reduce((t1, t2) -> t1 + t2)
            .orElse(0d);
    }
}
