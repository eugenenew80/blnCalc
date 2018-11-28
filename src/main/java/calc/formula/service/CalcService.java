package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.formula.CalcContext;
import calc.formula.CalcProperty;
import calc.formula.CalcResult;

public interface CalcService {
    CalcResult calcMeteringPoint(MeteringPoint point, Parameter param, CalcContext context) throws Exception;
    CalcResult calcMeteringPoint(MeteringPoint point, Parameter param, CalcContext context, CalcProperty property) throws Exception;

    CalcResult calcMeteringPoint(Formula formula, CalcContext context) throws Exception;
    CalcResult calcMeteringPoint(Formula formula, CalcContext context, CalcProperty property) throws Exception;
}
