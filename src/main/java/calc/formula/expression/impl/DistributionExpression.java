package calc.formula.expression.impl;

import calc.entity.calc.distr.DistrResultLine;
import calc.entity.calc.enums.GridTypeEnum;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.DistributionService;
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
public class DistributionExpression implements DoubleExpression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Long electricityGroupId;
    private final Double rate;
    private final DistributionService service;
    private final CalcContext context;
    private final GridTypeEnum gridType;

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
        List<DistrResultLine> list = service.getValues(meteringPointCode, parameterCode, context);

        Double value = list.stream()
            .filter(t -> t.getParam() != null)
            .filter(t -> t.getParam().getCode().equals(parameterCode))
            .map(t -> {
                if (gridType == GridTypeEnum.OWN)
                    return (Optional.ofNullable(t.getOwnVal()).orElse(0d)  * Optional.of(rate).orElse(1d));

                if (gridType == GridTypeEnum.OTHER)
                    return (Optional.ofNullable(t.getOtherVal()).orElse(0d)  * Optional.of(rate).orElse(1d));

                if (gridType == GridTypeEnum.TOTAL)
                    return (Optional.ofNullable(t.getTotalVal()).orElse(0d)  * Optional.of(rate).orElse(1d));

                return null;
            })
            .findFirst()
            .orElse(null);

        return value;
    }
}
