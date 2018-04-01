package kz.kegoc.bln.calc.operand;

import kz.kegoc.bln.calc.expression.UnaryExpression;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import javax.script.*;
import java.util.Map;
import java.util.function.UnaryOperator;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaScriptOperand implements Operand {
    private final String src;
    private final Map<String, Operand> attributes;
    private final ScriptEngine engine;

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

    @Override
    public UnaryExpression andThen(UnaryOperator<Operand> operator) {
        return UnaryExpression.builder()
            .operand(this)
            .operator(operator)
            .build();
    }

}
