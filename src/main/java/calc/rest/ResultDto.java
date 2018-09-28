package calc.rest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of= {"formulaId"})
public class ResultDto {
    private Long formulaId;
    private String status;
    private String msg;
    private Double val;
}
