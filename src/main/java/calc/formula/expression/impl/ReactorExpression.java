package calc.formula.expression.impl;

import calc.entity.calc.Formula;
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
    private final Formula formula;
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
        return service.getDoubleAttribute(id, attr, context);
    }

    @Override
    public String stringValue() {
        return service.getStringAttribute(id, attr, context);
    }

    @Override
    public Formula getFormula() {
        return formula;
    }
}
