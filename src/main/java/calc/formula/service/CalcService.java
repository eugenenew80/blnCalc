package calc.formula.service;

import calc.controller.rest.dto.ResultDto;
import calc.formula.CalcContext;

public interface CalcService {
    ResultDto getResult(String formula, CalcContext context) throws Exception;
    void getResult(CalcContext context) throws Exception;
}
