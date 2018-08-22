package calc.formula.expression.impl;

import calc.formula.CalcResult;
import calc.entity.calc.SourceType;
import calc.formula.CalcContext;
import calc.formula.CalcTrace;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.AtTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AtTimeValueExpression implements DoubleExpression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final String per;
    private final Double rate;
    private final AtTimeValueService service;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Double doubleValue() {
        List<CalcResult> list = service.getValue(
            meteringPointCode,
            parameterCode,
            per,
            context
        );
        list.forEach(t -> t.setDoubleValue(t.getDoubleValue() * rate));

        CalcTrace calcTrace = trace(list);
        Double result = list.stream()
            .map(t -> t.getDoubleValue())
            .reduce((t1, t2) -> t1 + t2)
            .orElse(null);

        calcTrace.setValue(result);
        return result;
    }

    @Override
    public Double[] doubleValues() {
        return new Double[0];
    }

    @Override
    public Set<String> pointCodes() {
        return Stream.of(meteringPointCode)
            .collect(toSet());
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
