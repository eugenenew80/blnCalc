package calc.formula.expression.impl;

import calc.formula.expression.DoubleExpression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.Set;
import java.util.function.UnaryOperator;
import static java.util.stream.Collectors.toList;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UnaryExpression implements DoubleExpression {
    private final DoubleExpression expression;
    private final UnaryOperator<DoubleExpression> operator;
    private final String code;

    @Override
    public DoubleExpression doubleExpression() {
        return operator.apply(expression);
    }

    @Override
    public Double doubleValue() {
        return doubleExpression().doubleValue();
    }

    @Override
    public Double[] doubleValues() {
        return Arrays.stream(expression.doubleValues())
            .map(v -> DoubleValueExpression.builder().value(v).build())
            .map(t -> operator.apply(t).doubleValue())
            .collect(toList())
            .toArray(new Double[0]);
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public Set<String> codes() {
        return expression.codes();
    }
}
