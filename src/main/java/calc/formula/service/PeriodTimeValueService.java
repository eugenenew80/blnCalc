package calc.formula.service;

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
}
