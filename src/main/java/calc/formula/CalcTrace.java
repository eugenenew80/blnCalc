package calc.formula;

import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.SourceEnum;
import calc.entity.calc.enums.SourceSystemEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalcTrace {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Integer dataTypeCount;
    private final DataTypeEnum dataType;
    private final Integer sourceCount;
    private final SourceEnum source;
    private final Integer sourceSystemCount;
    private final SourceSystemEnum sourceSystem;
}
