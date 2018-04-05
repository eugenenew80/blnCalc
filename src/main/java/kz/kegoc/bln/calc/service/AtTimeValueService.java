package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;

public interface AtTimeValueService {
    Double getValue(String meteringPointCode, String parameterCode, String per, CalcContext context);
}
