package calc.formula.expression.impl;

import calc.formula.CalcResult;
import calc.entity.calc.Formula;
import calc.entity.calc.SourceType;
import calc.formula.CalcContext;
import calc.formula.CalcTrace;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.PeriodTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PeriodTimeValueExpression implements DoubleExpression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Double rate;
    private final String interval;
    private final Byte startHour;
    private final Byte endHour;
    private final PeriodTimeValueService service;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Double doubleValue() {
        Double[] values = doubleValues();
        Double value = 0d;
        for (Double v : values) {
            if (v!=null) value+=v;
        }

        return value;
    }

    @Override
    public Double[] doubleValues() {
        List<CalcResult> list = getValues();
        list.forEach(t -> t.setDoubleVal(t.getDoubleVal() * rate));

        Double[] result;
        Double[] sum = new Double[24];
        Double[] count = new Double[24];
        Double[] avg = new Double[24];
        Arrays.fill(sum, null);
        Arrays.fill(count, null);
        Arrays.fill(avg, null);

        CalcTrace calcTrace = trace(list);

        list.stream()
            .map(t -> t.getMeteringDate().toLocalDate())
            .distinct()
            .sorted()
            .forEach(d -> {
                list.stream()
                    .filter(t -> t.getMeteringDate().toLocalDate().equals(d))
                    .forEach(t -> {
                        if (t.getDoubleVal()!=null && !t.getDoubleVal().isNaN()) {
                            int ind = t.getMeteringDate().getHour();

                            if (sum[ind]==null || sum[ind].isNaN())
                                sum[ind] = 0d;

                            if (count[ind]==null || count[ind].isNaN())
                                count[ind] = 0d;

                            sum[ind] = sum[ind] + t.getDoubleVal();
                            count[ind] = count[ind] + 1;
                        }
                    });
            });

        for (int i=0; i<sum.length; i++)
            if (sum[i]!=null && count[i]!=null) avg[i] = sum[i] / count[i];

        if (parameterCode.equals("U")) result = avg;
        else result = sum;

        calcTrace.setValues(result);
        return result;
    }

    @Override
    public String code() {
        return meteringPointCode;
    }

    @Override
    public Set<String> codes() {
        return Stream.of(meteringPointCode)
            .collect(toSet());
    }

    private List<CalcResult> getValues() {
        return service.getValues(
            meteringPointCode,
            parameterCode,
            startHour,
            endHour,
            context
        );
    }

    @SuppressWarnings("Duplicates")
    private CalcTrace trace(List<CalcResult> list) {
        List<CalcTrace> traces = context.getTrace().get(meteringPointCode);
        if (traces == null)
            traces = new ArrayList<>();

        List<SourceType> sourceTypeList = list.stream()
            .map(t -> t.getSourceType())
            .distinct()
            .collect(toList());

        CalcTrace calcTrace = CalcTrace.builder()
            .sourceTypeCount(sourceTypeList.size())
            .meteringPointCode(meteringPointCode)
            .parameterCode(parameterCode)
            .build();

        traces.add(calcTrace);
        context.getTrace().putIfAbsent(meteringPointCode, traces);
        return calcTrace;
    }
}
