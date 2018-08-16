package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_power_transformer_values")
public class PowerTransformerValue {
    @Id
    @SequenceGenerator(name="calc_power_transformer_values_s", sequenceName = "calc_power_transformer_values_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_power_transformer_values_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_header_id")
    private BalanceSubstResultHeader header;

    @Column(name = "val")
    private Double val;

    @Column(name = "val_xx")
    private Double valXX;

    @Column(name = "val_n")
    private Double valN;

    @Column(name = "windings_number")
    private Double windingsNumber;

    @Column(name = "delta_pr")
    private Double deltaPXX;

    @Column(name = "unom_h")
    private Double unomH;

    @Column(name = "snom")
    private Double snom;

    @Column(name = "uavg")
    private Double uavg;

    @Column(name = "pkz_hm")
    private Double pkzHM;

    @Column(name = "pkz_hl")
    private Double pkzHL;

    @Column(name = "pkz_ml")
    private Double pkzML;

    @Column(name = "result_h")
    private Double resistH;

    @Column(name = "result_m")
    private Double resistM;

    @Column(name = "result_l")
    private Double resistL;

    @Column(name = "total_e_h")
    private Double totalEH;

    @Column(name = "total_ae_h")
    private Double totalAEH;

    @Column(name = "total_re_h")
    private Double totalREH;

    @Column(name = "total_e_m")
    private Double totalEM;

    @Column(name = "total_ae_m")
    private Double totalAEM;

    @Column(name = "total_re_m")
    private Double totalREM;

    @Column(name = "total_e_l")
    private Double totalEL;

    @Column(name = "total_ae_l")
    private Double totalAEL;

    @Column(name = "total_re_l")
    private Double totalREL;

    @Column(name = "operating_time")
    private Double operatingTime;

    @ManyToOne
    @JoinColumn(name = "power_transformer_id")
    private PowerTransformer transformer;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "input_mp_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "input_mp_id_h")
    private MeteringPoint meteringPointH;

    @ManyToOne
    @JoinColumn(name = "input_mp_id_m")
    private MeteringPoint meteringPointM;

    @ManyToOne
    @JoinColumn(name = "input_mp_id_l")
    private MeteringPoint meteringPointL;
}

