package calc.formula.service;

import calc.formula.CalcContext;

public interface AtTimeValueService {
    Double getValue(String meteringPointCode, String parameterCode, String src, String per, CalcContext context);
}
