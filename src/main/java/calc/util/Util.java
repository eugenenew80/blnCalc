package calc.util;

import calc.entity.DataTypeSupport;
import calc.entity.TemplateLine;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.DataTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcTrace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;

public class Util {
    public static <K, V> Function<K, V> first(Function<K, V> f) {
        return f;
    }

    public static Double round(Double value, Parameter param) {
        if (value == null || param == null) return value;
        double rounding =  Math.pow(10, Optional.ofNullable(param.getDigitsRounding()).orElse(0));
        return Math.round(value * rounding) / rounding;
    }

    public static Double round(Double value, int digits) {
        if (value == null) return value;
        double rounding =  Math.pow(10, digits);
        return Math.round(value * rounding) / rounding;
    }


    public static Map<String, String> buildMsgParams(MeteringPoint mp) {
        if (mp == null) return new HashMap<>();

        Map<String, String> msgParams = new HashMap<>();
        msgParams.put("point", mp.getCode());
        return msgParams;
    }

    public static Map<String, String> buildMsgParams(MeteringPoint mp, Parameter param) {
        Map<String, String> msgParams = new HashMap<>();
        if (mp != null)
            msgParams.put("point", mp.getCode());

        if (param != null)
            msgParams.put("param", param.getCode());

        return msgParams;
    }

    public static Map<String, String> buildMsgParams(TemplateLine line) {
        if (line == null) return new HashMap<>();

        Map<String, String> msgParams = new HashMap<>();
        msgParams.put("line", line.getLineNum().toString());
        return msgParams;
    }

    public static  DataTypeEnum getDocDataType(List<? extends DataTypeSupport> resultLines) {
        Map<DataTypeEnum, List<DataTypeSupport>> dataTypes = resultLines.stream()
            .filter(t -> t.getDataType() != null)
            .collect(groupingBy(DataTypeSupport::getDataType));

        if (dataTypes.containsKey(DataTypeEnum.OPER))
            return DataTypeEnum.OPER;
        else if (dataTypes.containsKey(DataTypeEnum.FACT))
            return DataTypeEnum.FACT;
        else if (dataTypes.containsKey(DataTypeEnum.FINAL))
            return DataTypeEnum.FINAL;
        else
            return null;
    }

    public static DataTypeEnum getRowDataType(MeteringPoint meteringPoint, CalcContext context) {
        DataTypeEnum dataType;
        List<CalcTrace> traces = context.getTraces().get(meteringPoint.getCode());
        dataType = traces != null && !traces.isEmpty()
            ? traces.get(0).getDataType()
            : null;
        return dataType;
    }
}
