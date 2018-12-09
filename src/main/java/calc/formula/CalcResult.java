package calc.formula;

import calc.entity.calc.*;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.entity.calc.enums.SourceEnum;
import calc.entity.calc.enums.SourceSystemEnum;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalcResult {
    private Formula formula;
    private String paramType;
    private MeteringPoint meteringPoint;
    private Parameter param;
    private Unit unit;
    private LocalDateTime meteringDate;
    private SourceType sourceType;
    private PeriodTypeEnum periodType;
    private DataTypeEnum dataType;
    private SourceEnum source;

    private Double doubleValue;
    private Double[] doubleValues;
    private String stringValue;

    public SourceSystemEnum getSourceSystem() {
        return SourceSystemEnum.valueOf(sourceType.getSourceSystemCode());
    }

}
