package calc.formula.expression.impl;

import calc.entity.calc.bs.pe.ReactorValue;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.ReactorValueService;
import lombok.AccessLevel;
import lombok.*;
import java.util.*;
import java.util.stream.Stream;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toSet;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReactorValueExpression implements DoubleExpression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Double rate;
    private final ReactorValueService service;
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

        List<ReactorValue> list = service.getValues(context.getHeader().getId(), meteringPointCode);

        Double value = list.stream()
            .map(t -> ofNullable(t.getVal()).orElse(0d) )
            .reduce((t1, t2) -> t1 + t2)
            .orElse(null);

        cachedValue = value;
        return value;
    }
}
