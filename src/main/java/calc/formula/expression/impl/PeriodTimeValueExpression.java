package calc.formula.expression.impl;

import calc.entity.PeriodTimeValue;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.PeriodTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.setAll;
import static java.util.stream.Collectors.toList;
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
        List<PeriodTimeValue> list = getValues();
        list.forEach(t -> t.setVal(t.getVal() * rate));

        return list.stream()
            .map(t -> t.getVal())
            .reduce((t1, t2) -> t1 + t2)
            .orElse(0d);
    }

    @Override
    public Double[] values() {
        List<PeriodTimeValue> list = getValues();
        list.forEach(t -> t.setVal(t.getVal() * rate));

        Double[] results = new Double[24];
        setAll(results, d -> 0d);

        list.forEach(t -> results[t.getMeteringDate().getHour()] = t.getVal());
        return results;
    }

    @Override
    public Set<String> meteringPoints() {
        return Stream.of(meteringPointCode)
            .collect(toSet());
    }

    private List<PeriodTimeValue> getValues() {
        return service.getValues(
            meteringPointCode,
            parameterCode,
            src,
            interval,
            startHour,
            endHour,
            context
        );
    }
}
