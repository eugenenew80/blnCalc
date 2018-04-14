package calc.formula.expression.impl;

import calc.entity.Formula;
import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DoubleValueExpression implements Expression {
    private final Double value;
    private final Double[] values;
    private final Formula formula;

    @Override
    public Expression expression() {
        return this;
    }

    @Override
    public Double value() {
        return value;
    }

    @Override
    public Double[] values() {
        return values != null ? values : Expression.super.values();
    }
}
