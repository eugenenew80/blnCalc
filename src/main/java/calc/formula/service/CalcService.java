package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.formula.CalcContext;
import calc.formula.CalcResult;

public interface CalcService {
    CalcResult calcMeteringPoint(MeteringPoint point, String param, CalcContext context) throws Exception;
    CalcResult calcMeteringPoint(MeteringPoint point, String param, Formula formula, CalcContext context) throws Exception;
}
