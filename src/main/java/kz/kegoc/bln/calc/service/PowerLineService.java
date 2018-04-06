package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;

public interface PowerLineService {
    Double getNumberAttribute(Long id, String code, String attr, CalcContext context);
}
