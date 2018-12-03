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
    private final DeterminingMethodEnum determiningMethod = DeterminingMethodEnum.MPV;

    @Builder.Default
    private final ContextType contextType = ContextType.DEFAULT;

    @Builder.Default
    private final GridTypeEnum gridType = GridTypeEnum.OWN;

    @Builder.Default
    private final ParamTypeEnum paramType = ParamTypeEnum.PT;

    @Builder.Default
    private final ProcessOrder processOrder = ProcessOrder.CALC_READ;

    private final ElectricityProducerGroup electricityGroup;
}
