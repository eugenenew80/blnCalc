package calc.formula.expression.impl;

import calc.entity.calc.bs.u.BalanceSubstResultULine;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.BalanceSubstResultUService;
import lombok.*;
import org.springframework.util.StringUtils;
import java.util.List;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UavgExpression implements DoubleExpression {
    private final String meteringPointCode;
    private final Double def;
    private final BalanceSubstResultUService service;
    private final CalcContext context;
    private Double cachedValue;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Double doubleValue() {
        if (cachedValue != null)
            return cachedValue;

        if (StringUtils.isEmpty(meteringPointCode))
            return def;

        List<BalanceSubstResultULine> list = service.getValues(context.getHeader().getId(), meteringPointCode);

        Double value = list.stream()
            .filter(t -> t.getVal() != null && t.getVal() != 0)
            .map(t -> t.getVal())
            .findFirst()
            .orElse(def);

        cachedValue = value;
        return value;
    }
}
