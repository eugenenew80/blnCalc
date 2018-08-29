package calc.entity.calc;

import calc.entity.calc.enums.FormulaTypeEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Immutable
@Table(name = "calc_formulas")
@NamedEntityGraph(name="Formula.allJoins", attributeNodes = {
    @NamedAttributeNode("meteringPoint")
})
public class Formula {
    @Id
    private Long id;

    @Column
    private String code;

    @Column(name="formula_type")
    @Enumerated(EnumType.STRING)
    private FormulaTypeEnum formulaType;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column
    private String description;

    @Column
    private String text;

    @Column(name = "text_dialog")
    private String textDialog;

    @ManyToOne
    @JoinColumn(name="metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "formula", fetch = FetchType.LAZY)
    private List<FormulaVar> vars;

    @Column(name="param_type")
    @Enumerated(EnumType.STRING)
    private ParamTypeEnum paramType;
}
