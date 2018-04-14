package calc.formula.service;

import calc.entity.PeriodTimeValue;
import calc.formula.CalcContext;
import java.util.List;

public interface CalcService {
    PeriodTimeValue calc(String formula, CalcContext context) throws Exception;
    List<PeriodTimeValue> calc(CalcContext context) throws Exception;
}
