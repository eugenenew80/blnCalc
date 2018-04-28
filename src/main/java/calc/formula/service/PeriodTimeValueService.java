package calc.formula.service;

import calc.entity.calc.SourceTypePriority;
import calc.formula.CalcContext;
import calc.formula.CalcResult;

import java.util.List;

public interface PeriodTimeValueService {
    List<CalcResult> getValues(
        String meteringPointCode,
        String parameterCode,
        Byte startHour,
        Byte endHour,
        CalcContext context
    );

    List<SourceTypePriority> getSourceTypes(String meteringPointCode, CalcContext context);
}
