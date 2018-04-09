package calc.formula.expression.impl;

import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UnaryExpression implements Expression {
    private final Expression expression;
    private final UnaryOperator<Expression> operator;

    @Override
    public Expression expression() {
        return operator.apply(expression);
    }

    @Override
    public Double value() {
        return expression().value();
    }

    @Override
    public Set<String> meteringPoints() {
        return expression.meteringPoints();
    }
}
