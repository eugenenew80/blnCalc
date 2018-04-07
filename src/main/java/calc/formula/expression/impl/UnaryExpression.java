package calc.formula.expression.impl;

import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UnaryExpression implements Expression {
    private final Expression expression;
    private final UnaryOperator<Expression> operator;

    @Override
    public Expression calc() {
        return operator.apply(expression);
    }

    @Override
    public Double getValue() {
        return calc().getValue();
    }
}
