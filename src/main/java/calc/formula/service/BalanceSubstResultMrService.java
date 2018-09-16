package calc.formula.service;

import calc.entity.calc.bs.mr.BalanceSubstResultMrLine;
import java.util.List;

public interface BalanceSubstResultMrService {
    List<BalanceSubstResultMrLine> getValues(
        Long headerId,
        String meteringPointCode
    );
}
