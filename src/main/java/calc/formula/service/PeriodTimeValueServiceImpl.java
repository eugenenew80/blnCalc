package calc.formula.service;

import calc.formula.CalcContext;
import calc.entity.MeteringPoint;
import calc.entity.Parameter;
import calc.entity.PeriodTimeValue;
import calc.repo.MeteringPointRepo;
import calc.repo.ParameterRepo;
import calc.repo.PeriodTimeValueRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PeriodTimeValueServiceImpl implements PeriodTimeValueService {
    private final PeriodTimeValueRepo repo;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public Double getValue(String meteringPointCode, String parameterCode, String interval, Byte startHour, Byte endHour, CalcContext context) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        Parameter parameter = parameterRepo.findByCodeAndParamType(parameterCode, "PT");

        if (meteringPoint == null && parameter == null)
            return 0d;

        LocalDateTime startDate = context.getStartDate();
        LocalDateTime endDate = context.getEndDate();

        if (interval.equals("c")) {
            startDate = context.getStartDate();
            endDate = context.getEndDate().minusHours(1);
        }

        if (interval.equals("m")) {
            startDate = context.getStartDate()
                .truncatedTo(ChronoUnit.DAYS)
                .minusDays(context.getStartDate().getDayOfMonth()-1);
            endDate = context.getEndDate().minusHours(1);
        }

        List<PeriodTimeValue> list = repo.findAllByMeteringPointIdAndParamIdAndMeteringDateBetween(
            meteringPoint.getId(),
            parameter.getId(),
            startDate,
            endDate
        );

        Double result = 0d;
        if (!list.isEmpty()) {
            result = list.stream()
                .filter(t -> t.getMeteringDate().getHour()>=startHour && t.getMeteringDate().getHour()<=endHour)
                .map(t -> t.getVal())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);
        }

        return result;
    }
}
