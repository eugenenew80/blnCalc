package calc.formula.service;

import calc.formula.CalcContext;

public interface WorkingHoursService {
    Double getWorkingHours(String objectType, Long objectId, CalcContext context);
}
