package calc.formula;

import calc.entity.calc.SourceType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalcResult {
    private Long meteringPointId;
    private Long paramId;
    private Long unitId;
    private LocalDateTime meteringDate;
    private Long interval;
    private Double doubleVal;
    private Double[] doubleValues;
    private String stringVal;
    private String paramType;
    private SourceType sourceType;
}
