package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;

public interface PowerTransformerService {
    Double getNumberAttribute(Long id, String attr, CalcContext context);
}
