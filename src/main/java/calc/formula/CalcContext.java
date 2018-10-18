package calc.formula;

import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
@Builder
public class CalcContext {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long headerId;
    private PeriodTypeEnum periodType;
    private Long orgId;
    private String orgName;
    private String energyObjectType;
    private Long energyObjectId;
    private String energyObjectName;
    private String docCode;
    private Long docId;
    private Exception exception;

    @Builder.Default
    private ContextType contextType = ContextType.DEFAULT;

    @Builder.Default
    private Map<String, Double> transformerValues = new HashMap<>();

    @Builder.Default
    private Map<String, List<CalcResult>> values = new  HashMap<>();

    @Builder.Default
    private Map<String, List<CalcTrace>> trace = new  HashMap<>();

    @Builder.Default
    private Map<Long, CalcResult> results = new  HashMap<>();

    //private boolean isMeteringReading;
    //private boolean isAsp;
}
