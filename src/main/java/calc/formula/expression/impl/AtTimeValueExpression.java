package calc.formula.expression.impl;

import calc.controller.rest.dto.Result;
import calc.entity.Formula;
import calc.entity.SourceType;
import calc.formula.CalcContext;
import calc.formula.CalcInfo;
import calc.formula.expression.Expression;
import calc.formula.service.AtTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AtTimeValueExpression implements Expression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Double rate;
    private final Formula formula;
    private final AtTimeValueService service;
    private final CalcContext context;

    @Override
    public Expression expression() {
        return this;
    }

    @Override
    public Double value() {
        List<Result> list = service.getValue(
            meteringPointCode,
            parameterCode,
            context
        );
        list.forEach(t -> t.setVal(t.getVal() * rate));

        CalcInfo calcInfo = trace(list);
        Double result = list.stream()
            .filter(t -> t.getSourceType().equals(calcInfo.getSourceType()))
            .map(t -> t.getVal())
            .reduce((t1, t2) -> t1 + t2)
            .orElse(null);

        calcInfo.setValue(result);
        return result;
    }

    @Override
    public Double[] values() {
        return new Double[0];
    }

    @Override
    public Set<String> meteringPoints() {
        return Stream.of(meteringPointCode)
            .collect(toSet());
    }

    private CalcInfo trace(List<Result> list) {
        List<CalcInfo> infoList = context.getTrace().get(formula.getId());
        if (infoList == null)
            infoList = new ArrayList<>();

        List<SourceType> sourceTypeList = list.stream()
            .map(t -> t.getSourceType())
            .distinct()
            .collect(toList());

        CalcInfo calcInfo = CalcInfo.builder()
            .sourceType(selectSourceType(sourceTypeList))
            .sourceTypeCount(sourceTypeList.size())
            .meteringPointCode(meteringPointCode)
            .parameterCode(parameterCode)
            .build();

        infoList.add(calcInfo);
        context.getTrace().putIfAbsent(formula.getId(), infoList);
        return calcInfo;
    }

    private SourceType selectSourceType(List<SourceType> sourceTypeList) {
        sourceTypeList.stream()
            .distinct()
            .collect(toList());

        if (!sourceTypeList.isEmpty())
            return sourceTypeList.get(0);

        return null;
    }
}
