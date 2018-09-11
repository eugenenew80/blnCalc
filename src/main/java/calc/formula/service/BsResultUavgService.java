package calc.formula.service;

import calc.entity.calc.bs.BalanceSubstResultULine;
import java.util.List;

public interface BsResultUavgService {
    List<BalanceSubstResultULine> getValues(
        Long headerId,
        String meteringPointCode
    );
}
