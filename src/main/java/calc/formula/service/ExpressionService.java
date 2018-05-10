package calc.formula.service;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;

import java.util.List;
import java.util.Map;

public interface ExpressionService {
    DoubleExpression parse(Formula formula, CalcContext context) throws Exception;
    DoubleExpression parse(Formula formula, String parameterCode, CalcContext context) throws Exception;
    List<String> sort(Map<String, DoubleExpression> expressions) throws Exception;
}
