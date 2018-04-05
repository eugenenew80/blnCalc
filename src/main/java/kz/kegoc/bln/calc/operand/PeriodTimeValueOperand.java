package kz.kegoc.bln.calc.operand;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.calc.expression.UnaryExpression;
import kz.kegoc.bln.calc.service.PeriodTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PeriodTimeValueOperand implements Operand {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Double rate;
    private final String per;
    private final Byte start;
    private final Byte end;
    private final PeriodTimeValueService service;
    private final CalcContext context;

    @Override
    public Double getValue() {
        return rate*service.getValue(meteringPointCode, parameterCode, context);
    }

    @Override
    public UnaryExpression andThen(UnaryOperator<Operand> operator) {
        return UnaryExpression.builder()
            .operand(this)
            .operator(operator)
            .build();
    }
}
