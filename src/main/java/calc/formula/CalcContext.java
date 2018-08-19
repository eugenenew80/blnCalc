package calc.formula;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter @Setter
@Builder
public class CalcContext {
    private LocalDate startDate;
    private LocalDate endDate;

    private Long orgId;
    private String orgName;
    private String reportType;
    private String reportName;
    private String energyObjectType;
    private Long energyObjectId;
    private String energyObjectName;

    private String docCode;
    private Long docId;

    private List<CalcResult> values;
    private Map<String, List<CalcTrace>> trace;
    private Map<Long, CalcResult> results;
}
