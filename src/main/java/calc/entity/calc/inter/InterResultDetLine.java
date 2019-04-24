package calc.entity.calc.inter;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.enums.DefMethodLoss;
import calc.entity.calc.enums.DefMethodValue;
import calc.entity.calc.enums.DeviceStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_result_det_lines")
public class InterResultDetLine {
    @Id
    @SequenceGenerator(name="calc_act_inter_pl_result_det_lines_s", sequenceName = "calc_act_inter_pl_result_det_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_act_inter_pl_result_det_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private InterResultHeader header;

    @ManyToOne
    @JoinColumn(name = "result_line_id")
    private InterResultLine line;

    @Column(name = "direction")
    private Long direction;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_1")
    private MeteringPoint meteringPoint1;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_2")
    private MeteringPoint meteringPoint2;

    @Column(name = "val_1")
    private Double val1;

    @Column(name = "val_2")
    private Double val2;

    @Column(name = "loss_val")
    private Double lossVal;

    @Column(name = "loss_val_1")
    private Double lossVal1;

    @Column(name = "loss_val_2")
    private Double lossVal2;

    @Column(name = "loss_proc_1")
    private Double lossProc1;

    @Column(name = "loss_proc_2")
    private Double lossProc2;

    @Column(name = "bound_val")
    private Double boundVal;

    @Column(name = "loss_val_1x")
    private Double lossVal1x;

    @Column(name = "loss_val_2x")
    private Double lossVal2x;

    @Column(name="mp1_status_code")
    @Enumerated(EnumType.STRING)
    private DeviceStatus mpStatus1;

    @Column(name="mp2_status_code")
    @Enumerated(EnumType.STRING)
    private DefMethodValue defMethodValue1;

    @Column(name="actually_dm_of_losses_code")
    @Enumerated(EnumType.STRING)
    private DefMethodLoss actualDefMethodLoss;

    @Column(name = "is_alternative_method")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isAlternativeMethod;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;
}
