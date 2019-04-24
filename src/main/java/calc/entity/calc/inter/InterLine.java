package calc.entity.calc.inter;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.TemplateLine;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.PowerLine;
import calc.entity.calc.enums.*;
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
public class InterLine implements TemplateLine {
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

    @Column(name = "is_include_in_total", insertable = false, updatable = false)
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isIncludeTotal;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_include_in_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isIncludeInTotal;

    @Column(name="mp1_status_code")
    @Enumerated(EnumType.STRING)
    private DeviceStatus mpStatus1;

    @Column(name="dmv_in_mp1_code")
    @Enumerated(EnumType.STRING)
    private DefMethodValue defMethodValue1;

    @Column(name = "display_all_modes_bypass1_id")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isDisplayAllModesBypass1;

    @ManyToOne
    @JoinColumn(name = "metering_point_bypass1_id")
    private MeteringPoint meteringPointBypass1;

    @Column(name="mp2_status_code")
    @Enumerated(EnumType.STRING)
    private DeviceStatus mpStatus2;

    @Column(name="dmv_in_mp2_code")
    @Enumerated(EnumType.STRING)
    private DefMethodValue defMethodValue2;

    @Column(name = "display_all_modes_bypass2_id")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isDisplayAllModesBypass2;

    @ManyToOne
    @JoinColumn(name = "metering_point_bypass2_id")
    private MeteringPoint meteringPointBypass2;

    @Column(name="dm_on_bound_code")
    @Enumerated(EnumType.STRING)
    private CalcMethodBound methodOnBound;

    @Column(name="dm_of_losses_code")
    @Enumerated(EnumType.STRING)
    private DefMethodLoss defMethodLoss;

    @Column(name = "avg_loss_factor")
    private Double avgLossFactor;

    @Column(name = "specific_power_losses")
    private Double specificPowerLosses;

    @Column(name = "loading_nonlinearity_factor")
    private Double loadingNonlinearityFactor;

    @Column(name = "mv_define_by_side")
    private Long mvDefineBySide;

    @Column(name="dm_of_loss_share_code")
    @Enumerated(EnumType.STRING)
    private DefMethodLossShare defMethodLossShare;

    @Column(name = "is_inverse_mp1")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse1;

    @Column(name = "is_inverse_mp2")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse2;
}
