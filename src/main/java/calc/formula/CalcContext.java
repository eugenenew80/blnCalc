package calc.formula;

import calc.entity.AtTimeValue;
import calc.entity.PeriodTimeValue;
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
    private List<PeriodTimeValue> ptValues;
    private List<AtTimeValue> atValues;
    private Map<Long, List<CalcInfo>> calcTrace;
}
