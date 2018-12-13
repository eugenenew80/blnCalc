package calc.formula.expression.impl;

import calc.entity.calc.seg.SegResultLine;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.SegResultService;
import lombok.AccessLevel;
import lombok.*;
import java.util.*;
import java.util.stream.Stream;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toSet;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SegExpression implements DoubleExpression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Double rate;
    private final SegResultService service;
    private final CalcContext context;
    private Double cachedValue;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Set<String> pointCodes() {
        return Stream.of(meteringPointCode).collect(toSet());
    }

    @Override
    public Double[] doubleValues() {
        return new Double[] { doubleValue() };
    }

    @Override
    public Double doubleValue() {
        if (cachedValue != null)
            return cachedValue;

        List<SegResultLine> list = service.getValues(context.getHeader().getId(), meteringPointCode, parameterCode);

        Double value = list.stream()
            .map(t ->  ofNullable(t.getVal()).orElse(0d) * ofNullable(rate).orElse(1d))
            .reduce((t1, t2) -> t1 + t2)
            .orElse(null);

        cachedValue = value;
        return value;
    }
}
