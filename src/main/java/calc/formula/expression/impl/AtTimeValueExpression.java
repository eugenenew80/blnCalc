package calc.formula.expression.impl;

import calc.entity.calc.AtTimeValue;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.AtTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Optional.*;
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
        List<AtTimeValue> list = service.getValue(meteringPointCode, parameterCode, per, context);

        Double result = list.stream()
            .map(AtTimeValue::toResult)
            .map(t -> t.getDoubleValue())
            .filter(t -> t != null)
            .reduce(this::sum)
            .orElse(null);

        return result;
    }

    private Double sum(Double t1, Double t2) {
        return (ofNullable(t1).orElse(0d) + ofNullable(t2).orElse(0d)) * ofNullable(rate).orElse(1d);
    }
}
