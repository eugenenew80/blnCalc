package calc.formula.service;

import calc.entity.calc.bs.pe.PowerTransformerValue;
import java.util.List;

public interface TransformerValueService {
    List<PowerTransformerValue> getValues(Long headerId, String meteringPointCode);
}
