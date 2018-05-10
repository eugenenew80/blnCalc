package calc.formula.service;

import calc.formula.CalcContext;

public interface PowerTransformerService {
    Double getDoubleAttribute(Long id, String attr, CalcContext context);
    String getStringAttribute(Long id, String attr, CalcContext context);
}
