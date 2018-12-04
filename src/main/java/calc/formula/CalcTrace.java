package calc.formula;

import calc.entity.calc.enums.DataTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalcTrace {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Integer dataTypeCount;
    private final DataTypeEnum dataType;
    private final ContextTypeEnum contextType;
}
