package calc.formula.service;

import calc.formula.CalcContext;

public interface ReactorService {
    Double getNumberAttribute(Long id, String attr, CalcContext context);
}
