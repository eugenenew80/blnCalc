package calc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_formula_params")
public class FormulaParameter {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name="formula_id")
    private Formula formula;

    @ManyToOne
    @JoinColumn(name="parameter_id")
    private Parameter parameter;
}
