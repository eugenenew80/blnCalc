package calc.formula.doc;

import calc.entity.calc.TaskParam;
import calc.formula.CalcResult;

import java.util.List;
import java.util.Map;

public interface DocService {
    Map<String, List<CalcResult>> calc(TaskParam taskParam) throws Exception;
}
