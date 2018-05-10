package calc.formula.service;

import calc.formula.CalcContext;

public interface MeteringPointService {
    Double getDoubleAttribute(Long id, String code, String attr, CalcContext context);
    String getStringAttribute(Long id, String code, String attr, CalcContext context);
}
