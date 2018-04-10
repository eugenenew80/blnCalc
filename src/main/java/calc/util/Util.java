package calc.util;

import java.util.function.Function;

public class Util {
    public static <K, V> Function<K, V> first(Function<K, V> f) {
        return f;
    }
}
