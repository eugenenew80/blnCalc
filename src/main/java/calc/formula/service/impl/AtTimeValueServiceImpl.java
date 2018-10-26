package calc.formula.service.impl;

import calc.entity.calc.MeterHistory;
import calc.formula.CalcResult;
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
import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AtTimeValueServiceImpl implements AtTimeValueService {
    private final AtTimeValueRepo atTimeValueRepo;
    private final MeterHistoryRepo meterHistoryRepo;

    @Override
    public List<CalcResult> getValue(String meteringPointCode, String parameterCode, String per, CalcContext context) {
        if (context.getValues().containsKey(meteringPointCode)) {
            List<CalcResult> list = context.getValues().get(meteringPointCode)
                .stream()
                .filter(t -> t.getParamType().equals("AT"))
                .filter(t -> t.getParam().getCode().equals(parameterCode))
                .collect(toList());

            if (!list.isEmpty()) return list;
        }

        return findValues(meteringPointCode, parameterCode, per, context)
            .stream()
            .map(AtTimeValue::toResult)
            .collect(toList());
    }

    private List<AtTimeValue> findValues(String meteringPointCode, String paramCode, String per, CalcContext context) {
        LocalDateTime date = per.equals("end")
            ? context.getEndDate().atStartOfDay().plusDays(1)
            : context.getStartDate().atStartOfDay();

        List<AtTimeValue> values = atTimeValueRepo.findByParam(meteringPointCode, paramCode, date);
        Double factor = getFactor(meteringPointCode, per, context);

        for (AtTimeValue value : values)
            if (value.getSourceType().getId().equals(1l) && value.getVal() != null)
                value.setVal(value.getVal() / factor);

        return values;
    }

    private Double getFactor(String meteringPointCode, String per, CalcContext context) {
        LocalDateTime date = per.equals("end")
            ? context.getEndDate().atStartOfDay().plusDays(1)
            : context.getStartDate().atStartOfDay();

        MeterHistory meterHistory = meterHistoryRepo.findFirstByMeteringPoint(meteringPointCode, date);
        return meterHistory != null ? meterHistory.getFactor() : 1d;
    }
}
