package calc.formula.service;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.formula.CalcContext;
import calc.formula.CalcProperty;
import calc.formula.CalcResult;

public interface CalcService {
    CalcResult calcValue(MeteringPoint point, Parameter param, CalcContext context);
    CalcResult calcValue(MeteringPoint point, Parameter param, CalcContext context, CalcProperty property);

    CalcResult calcValue(Formula formula, CalcContext context);
    CalcResult calcValue(Formula formula, CalcContext context, CalcProperty property);

    CalcResult readValue(MeteringPoint point, Parameter param, CalcContext context, CalcProperty property);
}
