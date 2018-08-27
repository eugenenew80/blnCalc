package calc.formula.service;

import calc.entity.calc.BalanceSubstResultULine;
import java.util.List;

public interface UavgService {
    List<BalanceSubstResultULine> getValues(
        Long headerId,
        String meteringPointCode
    );
}
