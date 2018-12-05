package calc.formula.service;

import calc.entity.calc.Parameter;
import java.util.Map;

public interface ParamService {
    Map<String, Parameter> getValues();
    Parameter getParam(String paramCode);
}
