package calc.formula.expression.impl;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.ReactorService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReactorExpression implements Expression {
    private final Long id;
    private final String attr;
    private final Formula formula;
    private final ReactorService service;
    private final CalcContext context;

    @Override
    public Expression expression() {
        return this;
    }

    @Override
    public Double value() {
        return service.getNumberAttribute(id, attr, context);
    }
}
