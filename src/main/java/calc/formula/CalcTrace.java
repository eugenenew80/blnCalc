package calc.formula;

import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.SourceEnum;
import lombok.Builder;
import lombok.Data;

import javax.xml.transform.Source;

@Data
@Builder
public class CalcTrace {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Integer dataTypeCount;
    private final DataTypeEnum dataType;
    private final Integer sourceCount;
    private final SourceEnum source;
    private final ContextTypeEnum contextType;
}
