package calc.formula.service;

import calc.entity.calc.asp.AspResultLine;
import java.util.List;

public interface AspResultService {
    List<AspResultLine> getValues(
        Long headerId,
        String meteringPointCode
    );
}
