package calc.formula.service;

import calc.formula.CalcContext;

public interface PowerLineService {
    Double getNumberAttribute(Long id, String code, String attr, CalcContext context);
}
