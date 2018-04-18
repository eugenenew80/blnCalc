package calc.formula.service;

import calc.controller.rest.dto.Result;
import calc.entity.SourceTypePriority;
import calc.formula.CalcContext;
import java.util.List;

public interface PeriodTimeValueService {
    List<Result> getValues(
        String meteringPointCode,
        String parameterCode,
        Byte startHour,
        Byte endHour,
        CalcContext context
    );

    List<SourceTypePriority> getSourceTypes(String meteringPointCode, CalcContext context);
}
