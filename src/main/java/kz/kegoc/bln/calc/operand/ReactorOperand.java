package kz.kegoc.bln.calc.operand;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.calc.expression.UnaryExpression;
import kz.kegoc.bln.calc.service.ReactorService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReactorOperand implements Operand {
    private final Long id;
    private final String attr;
    private final ReactorService service;
    private final CalcContext context;

    @Override
    public Double getValue() {
        return service.getNumberAttribute(id, attr, context);
    }

    @Override
    public UnaryExpression andThen(UnaryOperator<Operand> operator) {
        return UnaryExpression.builder()
            .operand(this)
            .operator(operator)
            .build();
    }
}
