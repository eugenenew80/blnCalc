package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.entity.MeteringPoint;
import kz.kegoc.bln.entity.Parameter;
import kz.kegoc.bln.entity.PeriodTimeValue;
import kz.kegoc.bln.repo.MeteringPointRepo;
import kz.kegoc.bln.repo.ParameterRepo;
import kz.kegoc.bln.repo.PeriodTimeValueRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PeriodTimeValueServiceImpl implements PeriodTimeValueService {
    private final PeriodTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public Double getValue(String meteringPointCode, String parameterCode, CalcContext context) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        Parameter parameter = parameterRepo.findByCodeAndParamType(parameterCode, "PT");

        if (meteringPoint == null && parameter == null)
            return 0d;

        List<PeriodTimeValue> list = repo.findAllByMeteringPointIdAndParamIdAndMeteringDateBetween(
            meteringPoint.getId(),
            parameter.getId(),
            context.getStart(),
            context.getEnd().minusMinutes(15)
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
