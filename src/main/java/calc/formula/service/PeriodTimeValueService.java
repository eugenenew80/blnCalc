package calc.formula.service;

import calc.entity.calc.PeriodTimeValue;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import java.util.List;

public interface PeriodTimeValueService {
    List<PeriodTimeValue> getValues(
        String meteringPointCode,
        String parameterCode,
        CalcContext context
    );

    List<PeriodTimeValue> getValues(
        String meteringPointCode,
        String parameterCode,
        Byte startHour,
        Byte endHour,
        CalcContext context
    );
}
