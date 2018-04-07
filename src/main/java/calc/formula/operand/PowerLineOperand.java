package calc.formula.operand;

import calc.formula.CalcContext;
import calc.formula.expression.UnaryExpression;
import calc.formula.service.PowerLineService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PowerLineOperand implements Operand {
    private final Long id;
    private final String code;
    private final String attr;
    private final PowerLineService service;
    private final CalcContext context;

    @Override
    public Double getValue() {
        return service.getNumberAttribute(id, code, attr, context);
    }

    @Override
    public UnaryExpression andThen(UnaryOperator<Operand> operator) {
        return UnaryExpression.builder()
            .operand(this)
            .operator(operator)
            .build();
    }
}
