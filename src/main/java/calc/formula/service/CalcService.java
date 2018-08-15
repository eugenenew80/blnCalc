package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.expression.DoubleExpression;

import java.util.List;

public interface CalcService {
    Formula createFormula(MeteringPoint meteringPoint, Parameter parameter, ParamTypeEnum paramType);
    String formulaToString(Formula formula);
    CalcResult calc(String formula, CalcContext context) throws Exception;
    CalcResult calc(DoubleExpression expression, CalcContext context) throws Exception;
    List<CalcResult> calc(CalcContext context) throws Exception;
}
