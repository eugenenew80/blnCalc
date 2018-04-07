package calc.formula.expression.impl;

import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.AtTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AtTimeValueExpression implements Expression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final String per;
    private final Double rate;
    private final AtTimeValueService service;
    private final CalcContext context;

    @Override
    public Expression calc() {
        return this;
    }

    @Override
    public Double getValue() {
        return rate*service.getValue(meteringPointCode, parameterCode, per, context);
    }
}
