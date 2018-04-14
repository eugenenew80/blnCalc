package calc.formula.expression.impl;

import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

import static java.util.stream.Collectors.toSet;

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

    @Override
    public Double[] values() {
        return expression().values();
    }

    @Override
    public Set<String> meteringPoints() {
        Set<String> set = expressions.stream()
            .flatMap(t -> t.meteringPoints().stream())
            .collect(toSet());

        return new TreeSet<>(set);
    }
}
