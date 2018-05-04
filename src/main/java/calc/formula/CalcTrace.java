package calc.formula;

import calc.entity.calc.SourceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalcTrace {
    private String meteringPointCode;
    private String parameterCode;
    private SourceType sourceType;
    private String status;
    private Integer sourceTypeCount;
    private Double value;
    private Double[] values;
}