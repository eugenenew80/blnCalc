package calc.formula.expression.impl;

import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.StringExpression;
import calc.formula.service.MeteringPointService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MeteringPointExpression implements DoubleExpression, StringExpression {
    private final Long id;
    private final String code;
    private final String attr;
    private final MeteringPointService service;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Double doubleValue() {
        return service.getDoubleAttribute(id, code, attr, context);
    }

    @Override
    public StringExpression stringExpression() {
        return this;
    }

    @Override
    public String stringValue() {
        return service.getStringAttribute(id, code, attr, context);
    }
}
