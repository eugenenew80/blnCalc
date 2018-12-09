package calc.formula.expression.impl;

import calc.entity.calc.PeriodTimeValue;
import calc.entity.calc.enums.*;
import calc.formula.*;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.PeriodTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Stream;
import static calc.entity.calc.enums.SourceEnum.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PeriodTimeValueExpression implements DoubleExpression {
    private static final Logger logger = LoggerFactory.getLogger(PeriodTimeValueExpression.class);
    private final String meteringPointCode;
    private final String parameterCode;
    private final PeriodTimeValueService service;
    private final CalcContext context;

    @Builder.Default
    private final Double rate = 1d;

    @Builder.Default
    private final Byte startHour = 0;

    @Builder.Default
    private final Byte endHour = 23;


    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Set<String> pointCodes() {
        return Stream.of(meteringPointCode).collect(toSet());
    }

    @Override
    public Double doubleValue() {
        Double[] values = doubleValues();

        Double value = null;
        for (Double v : values) {
            if (v != null) {
                if (value==null) value = 0d;
                value+=v;
            }
        }
        return value;
    }

    @Override
    public Double[] doubleValues() {
        Map<DataTypeEnum, List<CalcResult>> mapDataStatus = service.getValues(meteringPointCode, parameterCode, context)
            .stream()
            .filter(t -> t.getPeriodType() == context.getHeader().getPeriodType())
            .map(PeriodTimeValue::toResult)
            .filter(t -> t.getDataType() != null)
            .collect(groupingBy(CalcResult::getDataType));

        DataTypeEnum dataType = getDataType(mapDataStatus);
        List<CalcResult> list = dataType !=null ? mapDataStatus.get(dataType) : null;
        if (list == null || list.size() == 0)
            return new Double[0];

        Map<SourceEnum, List<CalcResult>> mapSource = list.stream()
            .collect(groupingBy(CalcResult::getSource));

        SourceEnum source = getSource(mapSource);
        list = source !=null ? mapSource.get(source) : null;
        if (list == null || list.size() == 0)
            return new Double[0];

        Map<SourceSystemEnum, List<CalcResult>> mapSourceSystem = list.stream()
            .collect(groupingBy(CalcResult::getSourceSystem));

        SourceSystemEnum sourceSystem = getSourceSystem(mapSourceSystem);
        list = sourceSystem != null ? mapSourceSystem.get(sourceSystem) : null;
        if (list == null || list.size() == 0)
            return null;

        if (context.isTraceEnabled()) {
            List<CalcTrace> traces = context.getTraces().getOrDefault(meteringPointCode, new ArrayList<>());
            CalcTrace trace = CalcTrace.builder()
                .meteringPointCode(meteringPointCode)
                .parameterCode(parameterCode)
                .dataType(dataType)
                .dataTypeCount(mapDataStatus.size())
                .source(source)
                .sourceCount(mapSource.size())
                .sourceSystemCount(mapSourceSystem.size())
                .sourceSystem(sourceSystem)
                .build();

            traces.add(trace);
            context.getTraces().putIfAbsent(meteringPointCode, traces);
        }

        Double doubleValue = list.stream()
            .map(t -> t.getDoubleValue())
            .filter(t -> t != null)
            .reduce(this::sum)
            .orElse(null);

        return new Double[] {doubleValue};
    }

    private Double sum(Double t1, Double t2) {
        return (ofNullable(t1).orElse(0d) + ofNullable(t2).orElse(0d)) * ofNullable(rate).orElse(1d);
    }

    private DataTypeEnum getDataType(Map<DataTypeEnum, List<CalcResult>> map) {
        if (map == null || map.size() ==0)
            return null;

        List<DataTypeEnum> dataTypes = getDataTypes();
        for (DataTypeEnum dataType : dataTypes)
            if (map.containsKey(dataType))
                return dataType;

        return null;
    }

    private SourceEnum getSource(Map<SourceEnum, List<CalcResult>> map) {
        if (map == null || map.size() == 0)
            return null;

        List<SourceEnum> sources = Arrays.asList(CALC_BALPS, CALC_ASP1, CALC_SVR, CONSUMPTION, DAILY_SHEET, CALC_INTER_LEP, CALC_SEG, DEFAULT);
        for (SourceEnum source : sources)
            if (map.containsKey(source))
                return source;

        return null;
    }

    private SourceSystemEnum getSourceSystem(Map<SourceSystemEnum, List<CalcResult>> map) {
        if (map == null || map.size() == 0)
            return null;

        List<SourceSystemEnum> sources = Arrays.asList(SourceSystemEnum.BIS, SourceSystemEnum.EMCOS, SourceSystemEnum.OIC);
        for (SourceSystemEnum source : sources)
            if (map.containsKey(source))
                return source;

        return null;
    }

    private List<DataTypeEnum> getDataTypes() {
        List<DataTypeEnum> dataTypes = Arrays.asList(DataTypeEnum.FINAL, DataTypeEnum.FACT, DataTypeEnum.OPER);
        if (!context.isUseDataTypePriority() || context.getHeader().getDataType() == null)
            return dataTypes;

        DataTypeEnum docDataType = context.getHeader().getDataType();
        switch (docDataType) {
            case FINAL:
                dataTypes = Arrays.asList(DataTypeEnum.FINAL);
                break;

            case FACT:
                dataTypes = Arrays.asList(DataTypeEnum.FACT, DataTypeEnum.FINAL);
                break;

            case OPER:
                dataTypes = Arrays.asList(DataTypeEnum.OPER, DataTypeEnum.FACT, DataTypeEnum.FINAL);
                break;

            default:
                dataTypes = emptyList();
        }

        return dataTypes;
    }
}
