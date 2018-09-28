package calc.rest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of= {"formulaId"})
public class FormulaDto {
    private Long formulaId;
}
