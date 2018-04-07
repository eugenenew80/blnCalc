package calc.formula.service;

import calc.formula.CalcContext;
import calc.entity.AtTimeValue;
import calc.entity.MeteringPoint;
import calc.entity.Parameter;
import calc.repo.AtTimeValueRepo;
import calc.repo.MeteringPointRepo;
import calc.repo.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtTimeValueServiceImpl implements AtTimeValueService {
    private final AtTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public Double getValue(String meteringPointCode, String parameterCode, String per, CalcContext context) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        Parameter parameter = parameterRepo.findByCodeAndParamType(parameterCode, "AT");

        if (meteringPoint == null && parameter == null)
            return 0d;

        LocalDateTime dateTime;
        if (per.equals("s"))
            dateTime = context.getStartDate();
        else if (per.equals("e"))
            dateTime = context.getEndDate();
        else
            dateTime = LocalDate.now().atStartOfDay();

        List<AtTimeValue> list = repo.findAllByMeteringPointIdAndParamIdAndMeteringDate(
            meteringPoint.getId(),
            parameter.getId(),
            dateTime
        );

        Double result = 0d;
        if (!list.isEmpty()) {
            result = list.stream()
                .map(t -> t.getVal())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);
        }

        return result;
    }
}
