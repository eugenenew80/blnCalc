package calc.formula.service;

import calc.controller.rest.dto.Result;
import calc.formula.CalcContext;
import java.util.List;

public interface AtTimeValueService {
    List<Result> getValue(
        String meteringPointCode,
        String parameterCode,
        String src,
        CalcContext context
    );
}
