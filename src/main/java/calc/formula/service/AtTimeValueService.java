package calc.formula.service;

import calc.entity.SourceTypePriority;
import calc.formula.CalcContext;
import calc.formula.CalcResult;

import java.util.List;

public interface AtTimeValueService {
    List<CalcResult> getValue(
        String meteringPointCode,
        String parameterCode,
        CalcContext context
    );

    List<SourceTypePriority> getSourceTypes(String meteringPointCode, CalcContext context);
}
