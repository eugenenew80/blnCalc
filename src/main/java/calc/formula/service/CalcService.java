package calc.formula.service;

import calc.controller.rest.dto.ResultDto;
import calc.entity.Value;
import calc.formula.CalcContext;
import java.util.List;

public interface CalcService {
    ResultDto getResult(String formula, CalcContext context) throws Exception;
    List<Value> getResult(CalcContext context) throws Exception;
}
