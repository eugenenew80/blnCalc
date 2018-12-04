package calc.formula.service;

import calc.entity.calc.PeriodTimeValue;
import calc.formula.CalcContext;
import java.util.List;

public interface PeriodTimeValueService {
    List<PeriodTimeValue> getValues(
        String meteringPointCode,
        String parameterCode,
        CalcContext context
    );
}
