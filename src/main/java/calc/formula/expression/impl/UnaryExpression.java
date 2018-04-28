package calc.formula.expression.impl;

import calc.entity.calc.Formula;
import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.Set;
import java.util.function.UnaryOperator;
import static java.util.stream.Collectors.toList;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UnaryExpression implements Expression {
    private final Expression expression;
    private final UnaryOperator<Expression> operator;
    private final Formula formula;

    @Override
    public Expression expression() {
        return operator.apply(expression);
    }

    @Override
    public Double value() {
        return expression().value();
    }

    @Override
    public Double[] values() {
        return Arrays.stream(expression.values())
            .map(v -> DoubleValueExpression.builder().value(v).build())
            .map(t -> operator.apply(t).value())
            .collect(toList())
            .toArray(new Double[0]);
    }

    @Override
    public Set<String> meteringPoints() {
        return expression.meteringPoints();
    }
}
