package calc.formula.service;

import calc.entity.calc.bs.pe.ReactorValue;
import java.util.List;

public interface ReactorValueService {
    List<ReactorValue> getValues(Long headerId, String meteringPointCode);
}
