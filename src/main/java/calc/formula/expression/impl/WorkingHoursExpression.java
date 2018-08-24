package calc.formula.expression.impl;

import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.WorkingHoursService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkingHoursExpression implements DoubleExpression {
    private final String objectType;
    private final Long objectId;
    private final WorkingHoursService service;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Double doubleValue() {
        return service.getWorkingHours(objectType, objectId, context);
    }
}
