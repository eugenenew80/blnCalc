package calc.formula.service;

import calc.entity.calc.BypassMode;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.formula.CalcContext;

import java.util.List;

public interface MeteringReadingService {
    List<MeteringReading> calc(MeteringPoint meteringPoint, BypassMode bypassMode, Parameter parameter, CalcContext context);
}
