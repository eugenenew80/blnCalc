package calc.formula;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.SourceType;
import calc.entity.calc.Unit;
import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalcResult {
    private String paramType;
    private MeteringPoint meteringPoint;
    private Parameter param;
    private Unit unit;
    private LocalDateTime meteringDate;
    private SourceType sourceType;
    private PeriodTypeEnum periodType;

    private Double doubleValue;
    private Double[] doubleValues;
    private String stringValue;
}
