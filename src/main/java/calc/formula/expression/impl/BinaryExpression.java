package calc.formula.expression.impl;

import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.function.BinaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BinaryExpression implements Expression {
    private final List<Expression> expressions;
    private final BinaryOperator<Expression> operator;

    @Override
    public Expression expression() {
        return expressions.stream()
            .reduce(operator)
            .orElse(null);
    }

    @Override
    public Double value() {
        return expression().value();
    }
}
