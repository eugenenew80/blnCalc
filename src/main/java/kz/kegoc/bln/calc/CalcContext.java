package kz.kegoc.bln.calc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@EqualsAndHashCode(of= {"start", "end", "date"})
@Getter @Setter
public class CalcContext {
    private LocalDateTime start;
    private LocalDateTime end;
}
