package calc.formula.expression.impl;

import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.StringExpression;
import calc.formula.service.ReactorService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReactorExpression implements DoubleExpression, StringExpression {
    private final Long id;
    private final String attr;
    private final Double def;
    private final ReactorService service;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public StringExpression stringExpression() {
        return this;
    }

    @Override
    public Double doubleValue() {
        Double value = service.getDoubleAttribute(id, attr, context);
        return value != null ? value: def;
    }

    @Override
    public String stringValue() {
        return service.getStringAttribute(id, attr, context);
    }
}
