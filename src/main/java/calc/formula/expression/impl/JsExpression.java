package calc.formula.expression.impl;

import calc.formula.expression.DoubleExpression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import javax.script.*;
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
    private final String code;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public Set<String> codes() {
        Set<String> set = attributes.values().stream()
            .flatMap(t -> t.codes().stream())
            .collect(toSet());

        return new TreeSet<>(set);
    }

    @Override
    public Double[] doubleValues() {
        if (src.equals("a0") && attributes.size()==1)
            return attributes.get("a0").doubleValues();

        return DoubleExpression.super.doubleValues();
    }

    @Override
    public Double doubleValue() {
        if (src.equals("a0") && attributes.size()==1)
            return attributes.get("a0").doubleValue();

        Object eval = null;
        try {
            final ScriptContext ctx = new SimpleScriptContext();
            attributes.keySet().stream()
                .forEach(key -> ctx.setAttribute(key, attributes.get(key).doubleValue(), ScriptContext.ENGINE_SCOPE));

            eval = engine.eval(src, ctx);
        }
        catch (ScriptException e) {
            e.printStackTrace();
        }

        return eval!=null ? Double.parseDouble(eval.toString()) : null;
    }
}
