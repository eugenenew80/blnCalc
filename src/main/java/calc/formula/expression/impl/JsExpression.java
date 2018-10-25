package calc.formula.expression.impl;

import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static java.util.stream.Collectors.toCollection;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JsExpression implements DoubleExpression {
    private static final Logger logger = LoggerFactory.getLogger(JsExpression.class);
    private final String src;
    private final Map<String, DoubleExpression> attributes;
    private final ScriptEngine engine;
    private final CalcContext context;

    @Override
    public DoubleExpression doubleExpression() {
        return this;
    }

    @Override
    public Set<String> pointCodes() {
        return attributes
            .values()
            .stream()
            .flatMap(t -> t.pointCodes().stream())
            .collect(toCollection(TreeSet::new));
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
            attributes.keySet()
                .stream()
                .forEach(key -> ctx.setAttribute(key, attrs.get(key)[ind], ScriptContext.ENGINE_SCOPE));

            results[i] = eval(src, ctx);
        }
        return results;
    }

    @Override
    public Double doubleValue() {
        if (src.equals("a0") && attributes.size()==1) return attributes.get("a0").doubleValue();

        logger.trace("eval js expression:");
        logger.trace("  src: " +src);

        final ScriptContext ctx = new SimpleScriptContext();
        attributes.keySet()
            .stream()
            .forEach(key ->  {
                Double value = attributes.get(key).doubleValue();
                ctx.setAttribute(key, value, ScriptContext.ENGINE_SCOPE);
                logger.trace("  " + key + " : " + value);
            });

        Double eval = eval(src, ctx);
        logger.trace(eval !=null ?  "  val: " + eval.toString() : null);
        return eval;
    }

    private Double eval(String src, ScriptContext ctx) {
        try {
            context.setException(null);
            Object eval = engine.eval(src, ctx);
            return eval!=null ? Double.parseDouble(eval.toString()) : null;
        }
        catch (ScriptException e) {
            context.setException(e);
            logger.trace(e.getClass().getCanonicalName(), e);
        }
        return null;
    }
}
