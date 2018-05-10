package calc.formula.builder.xml;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import org.w3c.dom.Node;

public interface ExpressionBuilder<T extends DoubleExpression> {
    T build(Node node, Formula formula, CalcContext context);

    default T build(Node node, Formula formula, String parameterCode, CalcContext context) {
        return build(node, formula, context);
    }
}
