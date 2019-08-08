package calc.entity.calc.asp;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.*;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.TreatmentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_result_lines")
public class AspResultLine {
    @Id
    @SequenceGenerator(name="calc_asp1_result_lines_s", sequenceName = "calc_asp1_result_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_result_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_result_header_id")
    private AspResultHeader header;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "meter_id")
    private Meter meter;

    @ManyToOne
    @JoinColumn(name = "meter_history_id")
    private MeterHistory meterHistory;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @Column(name = "start_metering_date")
    private LocalDateTime startMeteringDate;

    @Column(name = "end_metering_date")
    private LocalDateTime endMeteringDate;

    @Column(name = "start_val")
    private Double startVal;

    @Column(name = "end_val")
    private Double endVal;

    @Column(name = "delta")
    private Double delta;

    @Column(name = "meter_rate")
    private Double meterRate;

    @Column(name = "val")
    private Double val;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "bypass_metering_point_id")
    private MeteringPoint bypassMeteringPoint;

    @ManyToOne
    @JoinColumn(name = "bypass_mode_id")
    private BypassMode bypassMode;

    @Column(name = "under_count_val")
    private Double underCountVal;

    @ManyToOne
    @JoinColumn(name = "under_count_id")
    private UnderCount undercount;

    @Column(name="treatment_type_code")
    @Enumerated(EnumType.STRING)
    private TreatmentTypeEnum treatmentType;

    @Column(name = "is_bold")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBold;

    @Column(name = "is_info")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInfo;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AspResultLineTranslate> translates;
}
