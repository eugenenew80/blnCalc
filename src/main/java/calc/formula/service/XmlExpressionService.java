package calc.formula.service;

import calc.entity.Formula;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import org.w3c.dom.Node;

public interface XmlExpressionService extends ExpressionService {
    Expression parse(Node node, Formula formula, String parameterCode, CalcContext context);
}
