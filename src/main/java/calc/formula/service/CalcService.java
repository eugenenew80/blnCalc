package calc.formula.service;

import calc.controller.rest.dto.Result;
import calc.formula.CalcContext;

import java.util.List;

public interface CalcService {
    Result calc(String formula, CalcContext context) throws Exception;
    List<Result> calc(CalcContext context) throws Exception;
}
