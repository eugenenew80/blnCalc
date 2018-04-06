package kz.kegoc.bln.calc;

import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(of= {"start", "end", "date"})
@Getter
@Builder
public class CalcContext {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
