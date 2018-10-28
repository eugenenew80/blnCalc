package calc.entity.calc.bs.mr;

import calc.entity.calc.*;
import calc.entity.calc.Parameter;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_bp_lines")
public class BalanceSubstResultBpLine {
    @Id
    @SequenceGenerator(name="calc_bs_result_bp_lines_s", sequenceName = "calc_bs_result_bp_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_bp_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_header_id")
    private BalanceSubstResultHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "bypass_metering_point_id")
    private MeteringPoint bypassMeteringPoint;

    @ManyToOne
    @JoinColumn(name = "bypass_mode_id")
    private BypassMode bypassMode;

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

    @Column(name = "bypass_start_date")
    private LocalDateTime bypassStartDate;

    @Column(name = "bypass_end_date")
    private LocalDateTime bypassEndDate;

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
}
