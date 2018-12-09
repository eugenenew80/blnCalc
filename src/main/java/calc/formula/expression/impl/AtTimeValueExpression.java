package calc.formula.expression.impl;

import calc.entity.calc.AtTimeValue;
import calc.entity.calc.enums.SourceSystemEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.AtTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import static java.util.Optional.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AtTimeValueExpression implements DoubleExpression {
    private static final Logger logger = LoggerFactory.getLogger(AtTimeValueExpression.class);
    private final String meteringPointCode;
    private final String parameterCode;
    private final String per;
    private final Double rate;
    private final AtTimeValueService service;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Set<String> pointCodes() {
        return Stream.of(meteringPointCode).collect(toSet());
    }

    @Override
    public Double[] doubleValues() {
        return new Double[0];
    }

    @Override
    public Double doubleValue() {
        Stream<AtTimeValue> stream = service.getValue(meteringPointCode, parameterCode, per, context)
            .stream();

        Map<SourceSystemEnum, List<CalcResult>> map = stream
            .map(AtTimeValue::toResult)
            .collect(groupingBy(CalcResult::getSourceSystem));

        SourceSystemEnum source = getSourceSystem(map);
        List<CalcResult> list = source !=null ? map.get(source) : null;
        if (list == null || list.size() == 0)
            return null;

        Double result = list.stream()
            .map(t -> t.getDoubleValue())
            .filter(t -> t != null)
            .reduce(this::sum)
            .orElse(null);

        return result;
    }

    private Double sum(Double t1, Double t2) {
        return (ofNullable(t1).orElse(0d) + ofNullable(t2).orElse(0d)) * ofNullable(rate).orElse(1d);
    }

    private SourceSystemEnum getSourceSystem(Map<SourceSystemEnum, List<CalcResult>> map) {
        if (map == null || map.size() ==0)
            return null;

        List<SourceSystemEnum> sources = Arrays.asList(SourceSystemEnum.BIS, SourceSystemEnum.EMCOS, SourceSystemEnum.OIC);
        for (SourceSystemEnum source : sources)
            if (map.containsKey(source))
                return source;

        return null;
    }
}
