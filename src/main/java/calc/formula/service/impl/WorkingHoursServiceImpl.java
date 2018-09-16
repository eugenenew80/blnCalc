package calc.formula.service.impl;

import calc.entity.calc.MeteringPointMode;
import calc.entity.calc.interfaces.Period;
import calc.entity.calc.PowerTransformerMode;
import calc.entity.calc.ReactorMode;
import calc.formula.CalcContext;
import calc.formula.service.WorkingHoursService;
import calc.repo.calc.MeteringPointModeRepo;
import calc.repo.calc.PowerTransformerModeRepo;
import calc.repo.calc.ReactorModeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkingHoursServiceImpl implements WorkingHoursService {
    private final MeteringPointModeRepo meteringPointModeRepo;
    private final ReactorModeRepo reactorModeRepo;
    private final PowerTransformerModeRepo transformerModeRepo;

    @Override
    public Double getWorkingHours(String objectType, Long objectId, CalcContext context) {
        LocalDateTime startDate = context.getStartDate().atStartOfDay();
        LocalDateTime endDate = context.getEndDate().atStartOfDay().plusDays(1);

        if (objectType.equals("mp")) {
            List<MeteringPointMode> modes = meteringPointModeRepo.findAllByMeteringPointIdAndDate(objectId, startDate, endDate);
            return getWorkingHours(modes, startDate, endDate);
        }

        if (objectType.equals("re")) {
            List<ReactorMode> modes = reactorModeRepo.findAllByReactorIdAndDate(objectId, startDate, endDate);
            return getWorkingHours(modes, startDate, endDate);
        }

        if (objectType.equals("tr")) {
            List<PowerTransformerMode> modes = transformerModeRepo.findAllByTransformerIdAndDate(objectId, startDate, endDate);
            return getWorkingHours(modes, startDate, endDate);
        }

        return 0d;
    }

    private <T extends Period> Double getWorkingHours(List<T> periods, LocalDateTime startDate, LocalDateTime endDate) {
        Double hours = getHoursBetween(startDate, endDate);
        for (Period period : periods) {
            LocalDateTime modeStartDate = Optional.ofNullable(period.getStartDate())
                .orElse(LocalDateTime.MIN);

            LocalDateTime modeEndDate = Optional.ofNullable(period.getEndDate())
                .orElse(LocalDateTime.MAX);

            if (startDate.isAfter(modeStartDate))
                modeStartDate = startDate;

            if (modeEndDate.isAfter(endDate))
                modeEndDate = endDate;

            hours = hours - getHoursBetween(modeStartDate, modeEndDate);
        }
        return hours;
    }

    private Double getHoursBetween(LocalDateTime startDate, LocalDateTime endDate) {
        long hours = ChronoUnit.HOURS.between(startDate, endDate);
        long minutes = ChronoUnit.MINUTES.between(startDate, endDate)-60*hours;
        return Math.round((hours + minutes / 60d)*100d) / 100d;
    }
}
