package calc.formula.service;

import calc.entity.calc.inter.InterResultMrLine;
import java.util.List;

public interface InterResultMrService {
    List<InterResultMrLine> getValues(Long headerId, String meteringPointCode);
}
