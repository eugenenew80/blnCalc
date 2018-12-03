package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.formula.CalcContext;
import calc.formula.CalcProperty;
import calc.formula.CalcResult;

public interface CalcService {
    CalcResult calcValue(MeteringPoint point, Parameter param, CalcContext context) throws Exception;
    CalcResult calcValue(MeteringPoint point, Parameter param, CalcContext context, CalcProperty property) throws Exception;

    CalcResult calcValue(Formula formula, CalcContext context) throws Exception;
    CalcResult calcValue(Formula formula, CalcContext context, CalcProperty property) throws Exception;

    CalcResult readValue(MeteringPoint point, Parameter param, CalcContext context, CalcProperty property);
}
