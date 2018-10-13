package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import java.util.List;

public interface CalcService {
    CalcResult calcMeteringPoint(MeteringPoint point, String param, CalcContext context) throws Exception;

    List<CalcResult> calcFormulas(List<Formula> formulas, CalcContext context) throws Exception;
}
