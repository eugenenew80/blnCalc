package calc.formula.expression.impl;

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
    private final String code;

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
    public String code() {
        return code;
    }

    @Override
    public Set<String> codes() {
        Set<String> set = expressions.stream()
            .flatMap(t -> t.codes().stream())
            .collect(toSet());

        return new TreeSet<>(set);
    }
}
