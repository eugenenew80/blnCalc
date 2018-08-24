package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_mr_lines")
public class BalanceSubstResultMrLine {
    @Id
    @SequenceGenerator(name="calc_bs_result_mr_lines_s", sequenceName = "calc_calc_bs_result_mr_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_mr_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_header_id")
    private BalanceSubstResultHeader header;

    @Column(name = "section")
    private String section;

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

    @Column(name = "under_count_val")
    private Double underCountVal;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "is_ignore")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isIgnore;

    @ManyToOne
    @JoinColumn(name = "bypass_metering_point_id")
    private MeteringPoint bypassMeteringPoint;

    @Column(name = "is_bypass_bus_section")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBypassSection;

    @ManyToOne
    @JoinColumn(name = "under_count_id")
    private UnderCount undercount;
}
