package calc.formula.service;

import calc.controller.rest.dto.ResultDto;
import calc.formula.CalcContext;
import java.util.List;

public interface CalcService {
    ResultDto getResult(String formula, CalcContext context) throws Exception;
    List<ResultDto> getResult(CalcContext context) throws Exception;
}
