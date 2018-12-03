package calc.formula.service.impl;

import calc.entity.calc.MeterHistory;
import calc.formula.CalcContext;
import calc.entity.calc.AtTimeValue;
import calc.formula.service.AtTimeValueService;
import calc.repo.calc.AtTimeValueRepo;
import calc.repo.calc.MeterHistoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AtTimeValueServiceImpl implements AtTimeValueService {
    private final AtTimeValueRepo atTimeValueRepo;
    private final MeterHistoryRepo meterHistoryRepo;

    @Override
    public List<AtTimeValue> getValue(String meteringPointCode, String parameterCode, String per, CalcContext context) {
        return findValues(meteringPointCode, parameterCode, per, context)
            .stream()
            .collect(toList());
    }

    private List<AtTimeValue> findValues(String meteringPointCode, String paramCode, String per, CalcContext context) {
        LocalDateTime date = per.equals("end")
            ? context.getHeader().getEndDate().atStartOfDay().plusDays(1)
            : context.getHeader().getStartDate().atStartOfDay();

        List<AtTimeValue> values = atTimeValueRepo.findByParam(meteringPointCode, paramCode, date);
        Double factor = getFactor(meteringPointCode, per, context);

        for (AtTimeValue value : values)
            if (value.getSourceType().getId().equals(1l) && value.getVal() != null)
                value.setVal(value.getVal() / factor);

        return values;
    }

    private Double getFactor(String meteringPointCode, String per, CalcContext context) {
        LocalDateTime date = per.equals("end")
            ? context.getHeader().getEndDate().atStartOfDay().plusDays(1)
            : context.getHeader().getStartDate().atStartOfDay();

        List<MeterHistory> meterHistory = meterHistoryRepo.findAllByMeteringPoint(meteringPointCode, date);
        if (meterHistory.size() > 1)
            throw new RuntimeException("Найдено больше одной запсиси в таблице mdfem_history");

        if (meterHistory == null) return 1d;
        if (meterHistory.isEmpty()) return 1d;
        if (meterHistory.get(0) == null) return 1d;
        if (meterHistory.get(0).getFactor() == null) return 1d;

        return meterHistory!= null && !meterHistory.isEmpty() ? Optional.of(meterHistory.get(0).getFactor()).orElse(1d) : 1d;
    }
}
