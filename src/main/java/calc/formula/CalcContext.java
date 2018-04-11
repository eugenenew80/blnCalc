package calc.formula;

import lombok.*;
import java.time.LocalDateTime;
import java.util.HashMap;

@Getter @Setter
@Builder
public class CalcContext {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long orgId;
    private HashMap<String, Double> values;
}
