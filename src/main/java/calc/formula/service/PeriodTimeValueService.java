package calc.formula.service;

import calc.formula.CalcContext;

public interface PeriodTimeValueService {
    Double getValue(String meteringPointCode, String parameterCode, String interval, Byte startHour, Byte endHour, CalcContext context);
}
