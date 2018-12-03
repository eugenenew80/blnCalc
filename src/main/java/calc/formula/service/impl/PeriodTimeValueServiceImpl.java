package calc.formula.service.impl;

import calc.entity.calc.PeriodTimeValue;
import calc.formula.CalcContext;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.calc.PeriodTimeValueRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PeriodTimeValueServiceImpl implements PeriodTimeValueService {
    private final PeriodTimeValueRepo repo;

    @Override
    public List<PeriodTimeValue> getValues(String meteringPointCode, String parameterCode, CalcContext context) {
        if (meteringPointCode == null || parameterCode == null)
            return emptyList();

        return findValues(meteringPointCode, parameterCode, context)
            .stream()
            .collect(toList());
    }

    private List<PeriodTimeValue> findValues(String meteringPointCode, String parameterCode, CalcContext context) {
        if (context == null || context.getHeader() == null)
            return emptyList();

        LocalDateTime startDate = context.getHeader()
            .getStartDate()
            .atStartOfDay();

        LocalDateTime endDate = context.getHeader()
            .getEndDate()
            .atStartOfDay()
            .plusDays(1)
            .minusHours(1);

        return repo.findByParam(
            meteringPointCode,
            parameterCode,
            startDate,
            endDate
        );
    }
}