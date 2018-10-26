package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.enums.DataLocationEnum;
import calc.entity.calc.enums.RowTypeEnum;
import calc.entity.calc.enums.TreatmentTypeEnum;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_unit_lines")
@Immutable
public class BalanceUnitLine {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_unit_id")
    private BalanceUnit header;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private BalanceUnitLine parent;

    @Column(name = "line_num")
    private Long lineNum;

    @Column(name = "row_type_code")
    @Enumerated(EnumType.STRING)
    private RowTypeEnum rowType;

    @Column(name = "treatment_type_code")
    @Enumerated(EnumType.STRING)
    private TreatmentTypeEnum treatmentType;

    @Column(name = "total_location_code")
    @Enumerated(EnumType.STRING)
    private DataLocationEnum dataLocation;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "is_inverse")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse;

    @Column(name = "is_bold")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBold;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY)
    private List<BalanceUnitLineTranslate> translates;
}
