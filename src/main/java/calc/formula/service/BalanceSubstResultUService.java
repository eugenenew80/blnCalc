package calc.formula.service;

import calc.entity.calc.bs.u.BalanceSubstResultULine;
import java.util.List;

public interface BalanceSubstResultUService {
    List<BalanceSubstResultULine> getValues(
        Long headerId,
        String meteringPointCode
    );
}
