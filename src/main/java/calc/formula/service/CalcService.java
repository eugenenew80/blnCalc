package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.exception.CycleDetectionException;
import calc.formula.expression.DoubleExpression;

import java.util.List;

public interface CalcService {
    CalcResult calc(DoubleExpression expression);

    CalcResult calc(String formula, CalcContext context) throws Exception;

    List<CalcResult> calc(List<Formula> formulas, CalcContext context) throws CycleDetectionException;

    List<CalcResult> calcMeteringPoints(List<MeteringPoint> points, CalcContext context) throws CycleDetectionException;

    DoubleExpression buildExpression(MeteringPoint meteringPoint, Parameter parameter, ParamTypeEnum paramType, PeriodTypeEnum periodType, CalcContext context);

    DoubleExpression buildExpression(Formula formula, CalcContext context);
}
