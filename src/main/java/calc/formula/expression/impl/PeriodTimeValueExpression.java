package calc.formula.expression.impl;

import calc.entity.calc.PeriodTimeValue;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.formula.CalcResult;
import calc.formula.CalcContext;
import calc.formula.CalcTrace;
import calc.formula.ContextTypeEnum;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.PeriodTimeValueService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Stream;

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
        Stream<PeriodTimeValue> stream = service.getValues(meteringPointCode, parameterCode, context)
            .stream()
            .filter(t -> t.getPeriodType() == context.getHeader().getPeriodType());

        Map<DataTypeEnum, List<CalcResult>> map = stream
            .map(PeriodTimeValue::toResult)
            .filter(t -> t.getDataType() != null)
            .collect(groupingBy(CalcResult::getDataType));

        DataTypeEnum dataType = getDataType(map);

        List<CalcResult> list = dataType !=null ? map.get(dataType) : null;
        if (list == null || list.size() == 0)
            return new Double[0];

        if (context.isTraceEnabled()) {
            List<CalcTrace> traces = context.getTraces().getOrDefault(meteringPointCode, new ArrayList<>());

            CalcTrace trace = CalcTrace.builder()
                .meteringPointCode(meteringPointCode)
                .parameterCode(parameterCode)
                .dataType(dataType)
                .dataTypeCount(map.size())
                .contextType(ContextTypeEnum.DEFAULT)
                .build();

            traces.add(trace);
            context.getTraces().putIfAbsent(meteringPointCode, traces);
        }

        if (context.getHeader().getPeriodType() == PeriodTypeEnum.H)
            return getByHours(list);

        Double doubleValue = list.stream()
            .map(t -> t.getDoubleValue())
            .filter(t -> t != null)
            .reduce(this::sum)
            .orElse(null);

        return new Double[] {doubleValue};
    }

    private Double[] getByHours(List<CalcResult> list) {
        Double[] result;
        Double[] sum = new Double[24];
        Double[] count = new Double[24];
        Double[] avg = new Double[24];
        Arrays.fill(sum, null);
        Arrays.fill(count, null);
        Arrays.fill(avg, null);

        list.stream()
            .map(t -> t.getMeteringDate().toLocalDate())
            .distinct()
            .sorted()
            .forEach(d -> {
                list.stream()
                    .filter(t -> t.getMeteringDate().toLocalDate().equals(d))
                    .forEach(t -> {
                        if (t.getDoubleValue()!=null && !t.getDoubleValue().isNaN()) {
                            int ind = t.getMeteringDate().getHour();

                            if (sum[ind]==null || sum[ind].isNaN())
                                sum[ind] = 0d;

                            if (count[ind]==null || count[ind].isNaN())
                                count[ind] = 0d;

                            sum[ind] = sum[ind] + t.getDoubleValue();
                            count[ind] = count[ind] + 1;
                        }
                    });
            });

        for (int i=0; i<sum.length; i++)
            if (sum[i]!=null && count[i]!=null) avg[i] = sum[i] / count[i];

        if (parameterCode.equals("U")) result = avg;
        else result = sum;

        return result;
    }

    private Double sum(Double t1, Double t2) {
        return (ofNullable(t1).orElse(0d) + ofNullable(t2).orElse(0d)) * ofNullable(rate).orElse(1d);
    }

    private DataTypeEnum getDataType(Map<DataTypeEnum, List<CalcResult>> map) {
        if (map == null || map.size() ==0)
            return null;

        List<DataTypeEnum> dataTypes = Arrays.asList(DataTypeEnum.FINAL, DataTypeEnum.FACT, DataTypeEnum.OPER);

        if (context.isUseDataTypePriority()) {
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
        }

        for (DataTypeEnum dataType : dataTypes)
            if (map.containsKey(dataType))
                return dataType;

        return null;
    }
}
