package calc.formula.service;

import calc.entity.calc.MeteringPoint;
import calc.formula.CalcContext;
import java.util.List;

public interface MrService {
    List<MeteringReading> calc(MeteringPoint meteringPoint, CalcContext context);
}
