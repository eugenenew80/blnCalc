package calc.formula;

import calc.entity.PeriodTimeValue;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@Builder
public class CalcContext {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long orgId;
    private List<PeriodTimeValue> ptValues;
}
