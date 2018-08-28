package calc.formula.expression.impl;

import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.BsResultUavgService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UavgExpression implements DoubleExpression {
    private final String meteringPointCode;
    private final Double def;
    private final BsResultUavgService service;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Double doubleValue() {
        if (meteringPointCode==null || meteringPointCode.equals(""))
            return def;

        return service.getValues(context.getHeaderId(), meteringPointCode).stream()
            .filter(t -> t.getVal() != null && t.getVal() != 0)
            .map(t -> t.getVal())
            .findFirst()
            .orElse(def);
    }
}
