package kz.kegoc.bln.calc.operand;

import kz.kegoc.bln.calc.expression.UnaryExpression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DoubleValueOperand implements Operand {
    private final Double value;

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public UnaryExpression andThen(UnaryOperator<Operand> operator) {
        return UnaryExpression.builder()
            .operand(this)
            .operator(operator)
            .build();
    }

}
