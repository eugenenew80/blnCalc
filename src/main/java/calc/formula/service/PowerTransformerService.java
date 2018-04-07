package calc.formula.service;

import calc.formula.CalcContext;

public interface PowerTransformerService {
    Double getNumberAttribute(Long id, String attr, CalcContext context);
}
