package calc.formula.service;

import calc.formula.CalcContext;

public interface PeriodTimeValueService {
    Double getValue(
        String meteringPointCode,
        String parameterCode,
        String src,
        String interval,
        Byte startHour,
        Byte endHour,
        CalcContext context
    );
}
