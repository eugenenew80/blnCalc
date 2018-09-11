package calc.formula.service.impl;

import calc.entity.calc.MeterHistory;
import calc.formula.CalcResult;
import calc.formula.CalcContext;
import calc.entity.calc.AtTimeValue;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.formula.service.AtTimeValueService;
import calc.repo.calc.AtTimeValueRepo;
import calc.repo.calc.MeterHistoryRepo;
import calc.repo.calc.MeteringPointRepo;
import calc.repo.calc.ParameterRepo;
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
    private final AtTimeValueRepo atTimeValueRepo;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;
    private final MeterHistoryRepo meterHistoryRepo;

    @Override
    public List<CalcResult> getValue(
        String meteringPointCode,
        String parameterCode,
        String per,
        CalcContext context
    ) {
        MeteringPoint meteringPoint = meteringPointRepo.findByCode(meteringPointCode);
        Parameter parameter = parameterRepo.findByCode(parameterCode);

        if (meteringPoint == null || parameter == null)
            return Collections.emptyList();

        if (context.getValues().containsKey(meteringPointCode)) {
            List<CalcResult> list = context.getValues().get(meteringPointCode)
                .stream()
                .filter(t -> t.getParamType().equals("AT"))
                .filter(t -> t.getParam().getCode().equals(parameter.getCode()))
                .collect(toList());

            if (!list.isEmpty())
                return list;
        }

        return findValues(meteringPoint, parameter, per, context)
            .stream()
            .map(AtTimeValue::toResult)
            .collect(toList());
    }

    private List<AtTimeValue> findValues(MeteringPoint meteringPoint, Parameter parameter, String per, CalcContext context) {
        LocalDateTime date = per.equals("end") ? context.getEndDate().atStartOfDay().plusDays(1) : context.getStartDate().atStartOfDay();

        List<AtTimeValue> values = atTimeValueRepo.findAllByMeteringPointIdAndParamIdAndMeteringDate(
            meteringPoint.getId(),
            parameter.getId(),
            date
        );

        Double factor = getFactor(meteringPoint, per, context);
        for (AtTimeValue value : values) {
            if (value.getSourceType().getId().equals(1l) && value.getVal()!=null)
                value.setVal(value.getVal() / factor);
        }

        return values;
    }

    private Double getFactor(MeteringPoint meteringPoint, String per, CalcContext context) {
        LocalDateTime date = per.equals("end") ? context.getEndDate().atStartOfDay().plusDays(1) : context.getStartDate().atStartOfDay();
        List<MeterHistory> meterHistoryList = meterHistoryRepo.findAllByMeteringPointIdAndDate(meteringPoint.getId(), date);
        return meterHistoryList.isEmpty() ? 1d : meterHistoryList.get(0).getFactor();
    }
}
