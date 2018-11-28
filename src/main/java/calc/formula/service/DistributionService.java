package calc.formula.service;

import calc.entity.calc.distr.DistrResultLine;
import calc.formula.CalcContext;
import java.util.List;

public interface DistributionService {
    List<DistrResultLine> getValues(
        String meteringPointCode,
        String parameterCode,
        CalcContext context
    );
}
