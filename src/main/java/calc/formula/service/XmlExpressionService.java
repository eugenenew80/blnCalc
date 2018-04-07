package calc.formula.service;

import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import org.w3c.dom.Node;

public interface XmlExpressionService extends ExpressionService {
    Expression parse(Node node, CalcContext context);
}
