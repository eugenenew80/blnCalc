package calc.util;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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

}
