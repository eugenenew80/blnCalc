package calc.formula.service;

import calc.entity.PeriodTimeValue;
import calc.formula.CalcContext;
import java.util.List;

public interface PeriodTimeValueService {
    List<PeriodTimeValue> getValues(
        String meteringPointCode,
        String parameterCode,
        String src,
        String interval,
        Byte startHour,
        Byte endHour,
        CalcContext context
    );

}
