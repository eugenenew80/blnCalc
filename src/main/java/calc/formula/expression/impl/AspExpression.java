package calc.formula.expression.impl;

import calc.entity.calc.AspResultLine;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.AspResultService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AspExpression implements DoubleExpression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Double rate;
    private final AspResultService service;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Double[] doubleValues() {
        return new Double[] { doubleValue() };
    }

    @Override
    public Set<String> pointCodes() {
        return Stream.of(meteringPointCode).collect(toSet());
    }

    @Override
    public Double doubleValue() {
        List<AspResultLine> list = service.getValues(
            context.getHeaderId(),
            meteringPointCode
        );

        Double value = list.stream()
            .filter(t -> t.getParam().getCode().equals(parameterCode))
            .map(t -> (Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d)) * rate)
            .reduce((t1, t2) -> t1 + t2)
            .orElse(null);

        return value;
    }
}
