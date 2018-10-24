package calc.entity.calc.inter;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_result_lines")
public class InterResultLine {
    @Id
    @SequenceGenerator(name="calc_act_inter_pl_result_lines_s", sequenceName = "calc_act_inter_pl_result_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_act_inter_pl_result_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private InterResultHeader header;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "power_line_id")
    private PowerLine powerLine;

    @ManyToOne
    @JoinColumn(name = "voltage_class_id")
    private VoltageClass voltageClass;

    @Column(name = "power_line_length")
    private Double powerLineLength;

    @Column(name = "is_bound_meter_inst")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBoundMeterInst;

    @ManyToOne
    @JoinColumn(name = "bound_metering_point_id")
    private MeteringPoint boundMeteringPoint;

    @Column(name = "boundary_val")
    private Double boundaryVal;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InterResultDetLine> details;
}
