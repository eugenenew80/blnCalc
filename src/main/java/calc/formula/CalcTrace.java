package calc.formula;

import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.SourceEnum;
import calc.entity.calc.enums.SourceSystemEnum;
import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class CalcTrace {
    private final String meteringPointCode;
    private final String parameterCode;
    private final Set<DataTypeEnum> dataTypes;
    private final DataTypeEnum dataType;
    private final Set<SourceEnum> sources;
    private final SourceEnum source;
    private final Set<SourceSystemEnum> sourceSystems;
    private final SourceSystemEnum sourceSystem;
}
