package calc.formula.expression.impl;

import calc.entity.calc.Formula;
import calc.formula.expression.DoubleExpression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BinaryExpression implements DoubleExpression {
    private final List<DoubleExpression> expressions;
    private final BinaryOperator<DoubleExpression> operator;
    private final Formula formula;

    @Override
    public DoubleExpression doubleExpression() {
        return expressions.stream()
            .reduce(operator)
            .orElse(null);
    }

    @Override
    public Double doubleValue() {
        return doubleExpression().doubleValue();
    }

    @Override
    public Double[] doubleValues() {
        return doubleExpression().doubleValues();
    }

    @Override
    public Set<String> meteringPoints() {
        Set<String> set = expressions.stream()
            .flatMap(t -> t.meteringPoints().stream())
            .collect(toSet());

        return new TreeSet<>(set);
    }
}
