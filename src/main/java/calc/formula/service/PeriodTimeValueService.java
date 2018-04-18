package calc.formula.service;

import calc.controller.rest.dto.Result;
import calc.formula.CalcContext;
import java.util.List;

public interface PeriodTimeValueService {
    List<Result> getValues(
        String meteringPointCode,
        String parameterCode,
        String src,
        Byte startHour,
        Byte endHour,
        CalcContext context
    );

}
