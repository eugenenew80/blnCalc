package calc.entity.calc.asp;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.TreatmentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_lines")
@Immutable
public class AspLine {
    @Id
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private AspHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @Column(name="treatment_type_code")
    @Enumerated(EnumType.STRING)
    private TreatmentTypeEnum treatmentType;

    @Column(name = "is_bold")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBold;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY)
    private List<AspLineTranslate> translates;
}
