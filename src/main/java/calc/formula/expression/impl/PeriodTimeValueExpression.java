package calc.formula.expression.impl;

import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.PeriodTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PeriodTimeValueExpression implements Expression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final String src;
    private final Double rate;
    private final String interval;
    private final Byte startHour;
    private final Byte endHour;
    private final PeriodTimeValueService service;
    private final CalcContext context;

    @Override
    public Expression expression() {
        return this;
    }

    @Override
    public Double value() {
        return rate*service.getValue(
            meteringPointCode,
            parameterCode,
            src,
            interval,
            startHour,
            endHour,
            context
        );
    }

    @Override
    public Set<String> meteringPoints() {
        return Stream.of(meteringPointCode)
            .collect(toSet());
    }
}
