package calc.formula.service;

import calc.entity.PeriodTimeValue;
import calc.formula.CalcContext;

public interface CalcService {
    PeriodTimeValue calc(String formula, CalcContext context) throws Exception;
    CalcContext calc(CalcContext context) throws Exception;
}
