package calc.entity.calc.seg;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.*;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.DataLocationEnum;
import calc.entity.calc.enums.RowTypeEnum;
import calc.entity.calc.enums.TreatmentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_result_lines")
public class SegResultLine {
    @Id
    @SequenceGenerator(name="calc_balance_segment_result_lines_s", sequenceName = "calc_balance_segment_result_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_result_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private SegResultHeader header;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private SegResultLine parent;

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

    @Column(name = "val")
    private Double val;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SegResultLineTranslate> translates;
}
