package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.expression.DoubleExpression;
import java.util.List;

public interface CalcService {
    CalcResult calcStr(String formula, CalcContext context) throws Exception;

    CalcResult calcExpression(DoubleExpression expression);

    List<CalcResult> calcMeteringPoints(List<MeteringPoint> points, String param, CalcContext context) throws Exception;

    List<CalcResult> calcFormulas(List<Formula> formulas, CalcContext context) throws Exception;
}
