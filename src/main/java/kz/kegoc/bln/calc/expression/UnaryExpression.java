package kz.kegoc.bln.calc.expression;

import kz.kegoc.bln.calc.operand.Operand;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UnaryExpression implements Expression {
    private final Operand operand;
    private final UnaryOperator<Operand> operator;

    @Override
    public Operand calc() {
        return operator.apply(operand);
    }

    @Override
    public Double getValue() {
        return calc().getValue();
    }

    @Override
    public UnaryExpression andThen(UnaryOperator<Operand> operator) {
        return UnaryExpression.builder()
            .operand(this)
            .operator(operator)
            .build();
    }
}
