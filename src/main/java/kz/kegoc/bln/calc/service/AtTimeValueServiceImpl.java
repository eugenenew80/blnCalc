package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.entity.AtTimeValue;
import kz.kegoc.bln.entity.MeteringPoint;
import kz.kegoc.bln.entity.Parameter;
import kz.kegoc.bln.repo.AtTimeValueRepo;
import kz.kegoc.bln.repo.MeteringPointRepo;
import kz.kegoc.bln.repo.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtTimeValueServiceImpl implements AtTimeValueService {
    private final AtTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public Double getValue(String meteringPointCode, String parameterCode, CalcContext context) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        Parameter parameter = parameterRepo.findByCodeAndParamType(parameterCode, "AT");

        if (meteringPoint == null && parameter == null)
            return 0d;

        List<AtTimeValue> list = repo.findAllByMeteringPointIdAndParamIdAndMeteringDate(
            meteringPoint.getId(),
            parameter.getId(),
            context.getDate()
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
