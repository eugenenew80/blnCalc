package calc.formula.expression.impl;

import calc.entity.calc.Formula;
import calc.formula.expression.DoubleExpression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DoubleValueExpression implements DoubleExpression {
    private final Double value;
    private final Double[] values;
    private final Formula formula;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Double doubleValue() {
        return value;
    }

    @Override
    public Double[] doubleValues() {
        return values != null ? values : DoubleExpression.super.doubleValues();
    }
}
