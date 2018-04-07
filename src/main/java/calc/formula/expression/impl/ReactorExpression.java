package calc.formula.expression.impl;

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
    private final ReactorService service;
    private final CalcContext context;

    @Override
    public Expression calc() {
        return this;
    }

    @Override
    public Double getValue() {
        return service.getNumberAttribute(id, attr, context);
    }
}
