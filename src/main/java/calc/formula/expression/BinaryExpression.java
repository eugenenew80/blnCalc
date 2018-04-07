package calc.formula.expression;

import calc.formula.operand.Operand;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BinaryExpression implements Expression {
    private final List<Operand> operands;
    private final BinaryOperator<Operand> operator;

    @Override
    public Operand calc() {
        return operands.stream()
            .reduce(operator)
            .orElseGet(null);
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
