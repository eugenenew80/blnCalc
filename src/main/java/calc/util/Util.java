package calc.util;

import calc.entity.DataTypeSupport;
import calc.entity.TemplateLine;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.DataTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcTrace;
import calc.formula.service.ParamService;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

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

    public static Map<String, String> buildMsgParams(Exception e) {
        HashMap<String, String> msgParams = new HashMap<>();
        msgParams.putIfAbsent("err", e.getMessage());
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

    public static DataTypeEnum getRowDataType(CalcContext context) {
        Set<DataTypeEnum> set = context.getTraces().values()
            .stream()
            .flatMap(t -> t.stream())
            .map(t -> t.getDataType())
            .collect(toSet());

        for (DataTypeEnum dataType : Arrays.asList(DataTypeEnum.OPER, DataTypeEnum.FACT, DataTypeEnum.FINAL))
            if (set.contains(dataType))
                return dataType;

        return null;
    }

    public static Parameter inverseParam(ParamService paramService, Parameter param, Boolean isInverse) {
        if (isInverse) {
            if (param.equals(paramService.getParam("A+"))) return paramService.getParam("A-");
            if (param.equals(paramService.getParam("A-"))) return paramService.getParam("A+");
            if (param.equals(paramService.getParam("R+"))) return paramService.getParam("R-");
            if (param.equals(paramService.getParam("R-"))) return paramService.getParam("R+");
        }
        return param;
    }
}
