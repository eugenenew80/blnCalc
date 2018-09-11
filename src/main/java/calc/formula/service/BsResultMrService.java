package calc.formula.service;

import calc.entity.calc.bs.BalanceSubstResultMrLine;
import java.util.List;

public interface BsResultMrService {
    List<BalanceSubstResultMrLine> getValues(
        Long headerId,
        String meteringPointCode
    );
}
