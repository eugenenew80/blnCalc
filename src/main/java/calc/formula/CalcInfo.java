package calc.formula;

import calc.entity.SourceType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CalcInfo {
    private SourceType sourceType;
    private String status;
}
