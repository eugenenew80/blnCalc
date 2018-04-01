package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;

public interface PeriodTimeValueService {
    Double getValue(String meteringPointCode, String parameterCode, CalcContext context);
}
