package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;

public interface CalcService {
    CalcResult calcMeteringPoint(MeteringPoint point, Parameter param, ParamTypeEnum paramType, CalcContext context) throws Exception;
    CalcResult calcMeteringPoint(Formula formula, CalcContext context) throws Exception;
}
