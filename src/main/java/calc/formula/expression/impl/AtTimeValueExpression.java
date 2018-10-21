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
import java.util.Optional;
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
    public DoubleExpression doubleExpression() { return this; }

    @Override
    public Set<String> pointCodes() { return Stream.of(meteringPointCode).collect(toSet()); }

    @Override
    public Double[] doubleValues() { return new Double[0]; }

    @Override
    public Double doubleValue() {
        List<CalcResult> list = service.getValue(meteringPointCode, parameterCode, per, context);

        Double result = list.stream()
            .map(t -> t.getDoubleValue())
            .filter(t -> t != null)
            .reduce((t1, t2) -> (Optional.ofNullable(t1).orElse(0d) + Optional.ofNullable(t2).orElse(0d)) * Optional.ofNullable(rate).orElse(1d))
            .orElse(null);

        return result;
    }
}
