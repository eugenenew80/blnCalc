package calc.formula.service;

import calc.entity.calc.MeteringPoint;
import calc.formula.CalcContext;
import java.util.List;

public interface MeteringReadingService {
    List<MeteringReading> calc(MeteringPoint meteringPoint, CalcContext context);
}
