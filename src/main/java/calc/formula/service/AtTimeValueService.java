package calc.formula.service;

import calc.entity.calc.AtTimeValue;
import calc.formula.CalcContext;
import java.util.List;

public interface AtTimeValueService {
    List<AtTimeValue> getValue(
        String meteringPointCode,
        String parameterCode,
        String per,
        CalcContext context
    );
}
