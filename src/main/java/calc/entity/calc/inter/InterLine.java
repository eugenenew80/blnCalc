package calc.entity.calc.inter;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.PowerLine;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_lines")
@Immutable
public class InterLine {
    @Id
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private InterHeader header;

    @ManyToOne
    @JoinColumn(name = "power_line_id")
    private PowerLine powerLine;

    @Column(name = "is_bound_meter_inst")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBoundMeterInst;

    @Column(name = "is_proportion_length")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isProportionLength;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_1")
    private MeteringPoint meteringPoint1;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_2")
    private MeteringPoint meteringPoint2;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_out_1")
    private MeteringPoint meteringPointOut1;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_out_2")
    private MeteringPoint meteringPointOut2;

    @ManyToOne
    @JoinColumn(name = "bound_metering_point_id")
    private MeteringPoint boundMeteringPoint;

    @Column(name = "proportion_1")
    private Double proportion1;

    @Column(name = "proportion_2")
    private Double proportion2;

    @Column(name = "power_line_length_1")
    private Double powerLineLength1;

    @Column(name = "power_line_length_2")
    private Double powerLineLength2;

    @Column(name = "power_line_length")
    private Double powerLineLength;

    @Column(name = "is_inverse")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse;

    @Column(name = "is_include_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isIncludeTotal;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
