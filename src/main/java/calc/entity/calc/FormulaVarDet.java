package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.enums.ParamTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Immutable
@Table(name = "calc_formula_var_det")
public class FormulaVarDet {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @ManyToOne
    @JoinColumn(name = "formula_var_id")
    private FormulaVar formulaVar;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column(name="param_type")
    @Enumerated(EnumType.STRING)
    private ParamTypeEnum paramType;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "sign")
    private String sign;
}
