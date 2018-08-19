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
    CalcResult calc(DoubleExpression expression);

    List<CalcResult> calc(List<DoubleExpression> expressions);

    CalcResult calc(String formula, CalcContext context) throws Exception;

    Formula createFormula(MeteringPoint meteringPoint, Parameter parameter, ParamTypeEnum paramType);

    String formulaToString(Formula formula);
}
