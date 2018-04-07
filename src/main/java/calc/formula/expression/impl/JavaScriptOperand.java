package calc.formula.expression.impl;

import calc.formula.expression.Expression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import javax.script.*;
import java.util.Map;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaScriptOperand implements Expression {
    private final String src;
    private final Map<String, Expression> attributes;
    private final ScriptEngine engine;

    @Override
    public Expression calc() {
        return this;
    }

    @Override
    public Double getValue() {
        Object eval = null;
        try {
            final ScriptContext ctx = new SimpleScriptContext();
            attributes.keySet().stream()
                .forEach(key -> ctx.setAttribute(key, attributes.get(key).getValue(), ScriptContext.ENGINE_SCOPE));

            eval = engine.eval(src, ctx);
        }
        catch (ScriptException e) {
            e.printStackTrace();
        }

        return eval!=null ? Double.parseDouble(eval.toString()) : null;
    }
}
