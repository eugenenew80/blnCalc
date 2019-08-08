package calc.formula;

import calc.entity.calc.ElectricityProducerGroup;
import calc.entity.calc.enums.DeterminingMethodEnum;
import calc.entity.calc.enums.GridTypeEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class CalcProperty {
    @Builder.Default
    private DeterminingMethodEnum determiningMethod = DeterminingMethodEnum.MPV;

    @Builder.Default
    private ContextTypeEnum contextType = ContextTypeEnum.DEFAULT;

    @Builder.Default
    private GridTypeEnum gridType = GridTypeEnum.OWN;

    @Builder.Default
    private ParamTypeEnum paramType = ParamTypeEnum.PT;

    @Builder.Default
    private ProcessOrderEnum processOrder = ProcessOrderEnum.CALC_READ;

    private ElectricityProducerGroup electricityGroup;

    @Builder.Default
    private int currentDeep =  0;
}
