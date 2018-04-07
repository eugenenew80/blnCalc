package calc.formula.builder.xml;

import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import org.w3c.dom.Node;

public interface ExpressionBuilder<T extends Expression> {
    T build(Node node, CalcContext context);
}
