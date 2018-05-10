package calc.formula.service;

import calc.formula.CalcContext;
import calc.formula.CalcResult;
import java.util.List;

public interface AtTimeValueService {
    List<CalcResult> getValue(
        String meteringPointCode,
        String parameterCode,
        String per,
        CalcContext context
    );
}
