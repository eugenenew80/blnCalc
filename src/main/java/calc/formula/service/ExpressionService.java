package calc.formula.service;

import calc.entity.Formula;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;

import java.util.List;
import java.util.Map;

public interface ExpressionService {
    Expression parse(Formula formula, CalcContext context) throws Exception;
    List<String> sort(Map<String, Expression> expressions) throws Exception;
}
