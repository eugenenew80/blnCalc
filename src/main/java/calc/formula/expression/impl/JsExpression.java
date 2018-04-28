package calc.formula.expression.impl;

import calc.entity.calc.Formula;
import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import javax.script.*;
import java.util.Map;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JsExpression implements Expression {
    private final String src;
    private final Map<String, Expression> attributes;
    private final ScriptEngine engine;
    private final Formula formula;

    @Override
    public Expression expression() {
        return this;
    }

    @Override
    public Double value() {
        Object eval = null;
        try {
            final ScriptContext ctx = new SimpleScriptContext();
            attributes.keySet().stream()
                .forEach(key -> ctx.setAttribute(key, attributes.get(key).value(), ScriptContext.ENGINE_SCOPE));

            eval = engine.eval(src, ctx);
        }
        catch (ScriptException e) {
            e.printStackTrace();
        }

        return eval!=null ? Double.parseDouble(eval.toString()) : null;
    }
}
