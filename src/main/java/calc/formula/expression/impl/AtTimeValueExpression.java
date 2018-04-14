package calc.formula.expression.impl;

import calc.entity.Formula;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.AtTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AtTimeValueExpression implements Expression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final String src;
    private final Double rate;
    private final Formula formula;
    private final AtTimeValueService service;
    private final CalcContext context;

    @Override
    public Expression expression() {
        return this;
    }

    @Override
    public Double value() {
        if (formula.getMultiValues())
            return 0d;

        return rate*service.getValue(
            meteringPointCode,
            parameterCode,
            src,
            context
        );
    }

    @Override
    public Double[] values() {
        if (!formula.getMultiValues())
            return new Double[0];

        Double v = value() / 24.0d;
        return new Double[] { v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v  };
    }

    @Override
    public Set<String> meteringPoints() {
        return Stream.of(meteringPointCode)
            .collect(toSet());
    }
}
