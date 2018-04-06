package kz.kegoc.bln.calc.operand;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.calc.expression.UnaryExpression;
import kz.kegoc.bln.calc.service.AtTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AtTimeValueOperand implements Operand {
    private final String meteringPointCode;
    private final String parameterCode;
    private final String per;
    private final Double rate;
    private final AtTimeValueService service;
    private final CalcContext context;

    @Override
    public Double getValue() {
        return rate*service.getValue(meteringPointCode, parameterCode, per, context);
    }

    @Override
    public UnaryExpression andThen(UnaryOperator<Operand> operator) {
        return UnaryExpression.builder()
            .operand(this)
            .operator(operator)
            .build();
    }
}
