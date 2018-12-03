package calc.formula.service;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;

public interface ExpressionService {
    //DoubleExpression parse(Formula formula, CalcContext context) throws Exception;
    DoubleExpression parse(Formula formula, String parameterCode, CalcContext context) throws Exception;
}
