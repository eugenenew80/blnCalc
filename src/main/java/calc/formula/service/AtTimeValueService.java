package calc.formula.service;

import calc.entity.calc.SourceTypePriority;
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

    List<SourceTypePriority> getSourceTypes(String meteringPointCode, CalcContext context);
}
