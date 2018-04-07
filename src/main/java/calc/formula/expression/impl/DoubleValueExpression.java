package calc.formula.expression.impl;

import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DoubleValueExpression implements Expression {
    private final Double value;

    @Override
    public Expression calc() {
        return this;
    }

    @Override
    public Double getValue() {
        return value;
    }
}
