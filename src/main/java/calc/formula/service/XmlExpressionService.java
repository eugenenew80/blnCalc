package calc.formula.service;

import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import org.w3c.dom.Node;

public interface XmlExpressionService extends ExpressionService {
    DoubleExpression parse(Node node, String parameterCode, CalcContext context);
}
