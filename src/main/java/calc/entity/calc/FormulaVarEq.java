package calc.entity.calc;

import calc.entity.calc.enums.EquipmentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Immutable
@Table(name = "calc_formula_var_eq")
public class FormulaVarEq {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @ManyToOne
    @JoinColumn(name = "formula_var_id")
    private FormulaVar formulaVar;

    @Column(name="equipment_type")
    @Enumerated(EnumType.STRING)
    private EquipmentTypeEnum equipmentType;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @ManyToOne
    @JoinColumn(name = "param_type")
    private EqParameter param;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "sign")
    private String sign;
}
