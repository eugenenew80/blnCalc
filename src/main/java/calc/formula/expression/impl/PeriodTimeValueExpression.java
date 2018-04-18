package calc.formula.expression.impl;

import calc.formula.CalcResult;
import calc.entity.Formula;
import calc.entity.SourceType;
import calc.formula.CalcContext;
import calc.formula.CalcTrace;
import calc.formula.expression.Expression;
import calc.formula.service.PeriodTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PeriodTimeValueExpression implements Expression {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Double rate;
    private final String interval;
    private final Byte startHour;
    private final Byte endHour;
    private final Formula formula;
    private final PeriodTimeValueService service;
    private final CalcContext context;

    @Override
    public Expression expression() {
        return this;
    }

    @Override
    public Double value() {
        return null;
    }

    @Override
    public Double[] values() {
        List<CalcResult> list = getValues();
        list.forEach(t -> t.setVal(t.getVal() * rate));

        Double[] results = new Double[24];
        Arrays.fill(results, null);

        CalcTrace calcInfo = trace(list);
        list.stream()
            .filter( t -> t.getSourceType().equals(calcInfo.getSourceType()) )
            .forEach(t -> results[t.getMeteringDate().getHour()] = t.getVal());

        calcInfo.setValues(results);
        return results;
    }

    @Override
    public Set<String> meteringPoints() {
        return Stream.of(meteringPointCode)
            .collect(toSet());
    }

    private List<CalcResult> getValues() {
        return service.getValues(
            meteringPointCode,
            parameterCode,
            startHour,
            endHour,
            context
        );
    }

    @SuppressWarnings("Duplicates")
    private CalcTrace trace(List<CalcResult> list) {
        List<CalcTrace> infoList = context.getTrace().get(formula.getId());
        if (infoList == null)
            infoList = new ArrayList<>();

        List<SourceType> sourceTypeList = list.stream()
            .map(t -> t.getSourceType())
            .distinct()
            .collect(toList());

        CalcTrace calcInfo = CalcTrace.builder()
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
