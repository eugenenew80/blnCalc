package calc.formula.service;

import calc.formula.CalcContext;
import calc.formula.expression.Expression;

public interface ExpressionService {
    Expression parse(String formula, CalcContext context) throws Exception;
}
