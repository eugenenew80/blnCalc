package calc.formula.builder.xml;

import calc.entity.Formula;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import org.w3c.dom.Node;

public interface ExpressionBuilder<T extends Expression> {
    T build(Node node, Formula formula, CalcContext context);

    default T build(Node node, Formula formula, String parameterCode, CalcContext context) {
        return build(node, formula, context);
    }
}
