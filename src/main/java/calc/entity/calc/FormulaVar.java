package calc.entity.calc;

import calc.entity.calc.enums.VarTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Immutable
@Table(name = "calc_formula_vars")
public class FormulaVar {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @Column(name = "var_name")
    private String varName;

    @Column
    private String description;

    @Column(name="var_type")
    @Enumerated(EnumType.STRING)
    private VarTypeEnum varType;

    @OneToMany(mappedBy = "formulaVar", fetch = FetchType.LAZY)
    private List<FormulaVarDet> details;

    @OneToMany(mappedBy = "formulaVar", fetch = FetchType.LAZY)
    private List<FormulaVarEq> equipments;
}
