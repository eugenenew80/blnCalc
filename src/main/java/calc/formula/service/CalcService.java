package calc.formula.service;

import calc.formula.CalcContext;
import calc.formula.CalcResult;

import java.util.List;

public interface CalcService {
    CalcResult calc(String formula, CalcContext context) throws Exception;
    List<CalcResult> calc(CalcContext context) throws Exception;
}
