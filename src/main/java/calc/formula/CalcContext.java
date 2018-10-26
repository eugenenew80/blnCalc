package calc.formula;

import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
@Builder
public class CalcContext {
    private LangEnum lang;
    private String docCode;
    private Long headerId;
    private PeriodTypeEnum periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long orgId;
    private String energyObjectType;
    private Long energyObjectId;
    private Exception exception;

    @Builder.Default
    private ContextType contextType = ContextType.DEFAULT;

    @Builder.Default
    private Map<String, Double> transformerValues = new HashMap<>();

    @Builder.Default
    private Map<String, List<CalcResult>> values = new  HashMap<>();

    @Builder.Default
    private Map<Long, CalcResult> results = new  HashMap<>();
}
