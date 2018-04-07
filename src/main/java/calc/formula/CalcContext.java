package calc.formula;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class CalcContext {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
