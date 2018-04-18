package calc.formula.service;

import calc.controller.rest.dto.Result;
import calc.entity.SourceTypePriority;
import calc.formula.CalcContext;
import java.util.List;

public interface AtTimeValueService {
    List<Result> getValue(
        String meteringPointCode,
        String parameterCode,
        CalcContext context
    );

    List<SourceTypePriority> getSourceTypes(String meteringPointCode, CalcContext context);
}
