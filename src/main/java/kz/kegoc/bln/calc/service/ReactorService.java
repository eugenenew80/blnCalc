package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;

public interface ReactorService {
    Double getNumberAttribute(Long id, String attr, CalcContext context);
}
