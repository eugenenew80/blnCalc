package calc.formula;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@Builder
public class CalcContext {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long orgId;
}
