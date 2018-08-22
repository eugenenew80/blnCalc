package calc.formula.expression.impl;

import calc.formula.expression.DoubleExpression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import javax.script.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static java.util.stream.Collectors.toSet;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JsExpression implements DoubleExpression {
    private final String src;
    private final Map<String, DoubleExpression> attributes;
    private final ScriptEngine engine;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Set<String> pointCodes() {
        Set<String> set = attributes
            .values()
            .stream()
            .flatMap(t -> t.pointCodes().stream())
            .collect(toSet());

        return new TreeSet<>(set);
    }

    @Override
    public Double[] doubleValues() {
        if (src.equals("a0") && attributes.size()==1) return attributes.get("a0").doubleValues();

        Map<String, Double[]> attrs = new HashMap<>();
        int count = 0;
        for (String key : attributes.keySet()) {
            Double[] values = attributes.get(key).doubleValues();
            if (values.length>count) count = values.length;
            attrs.put(key, values);
        }

        Double[] results = new Double[count];
        for (int i=0; i<count; i++) {
            final ScriptContext ctx = new SimpleScriptContext();
            int ind = i;
            attributes.keySet().stream().forEach(key -> ctx.setAttribute(key, attrs.get(key)[ind], ScriptContext.ENGINE_SCOPE));
            results[i] = eval(src, ctx);
        }

        return results;
    }

    @Override
    public Double doubleValue() {
        if (src.equals("a0") && attributes.size()==1) return attributes.get("a0").doubleValue();

        final ScriptContext ctx = new SimpleScriptContext();
        attributes.keySet().stream().forEach(key -> ctx.setAttribute(key, attributes.get(key).doubleValue(), ScriptContext.ENGINE_SCOPE));
        Double eval = eval(src, ctx);
        return eval;
    }

    private Double eval(String src, ScriptContext ctx) {
        try {
            Object eval = engine.eval(src, ctx);
            return eval!=null ? Double.parseDouble(eval.toString()) : null;
        }
        catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }
}
