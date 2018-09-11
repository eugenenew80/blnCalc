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
    private Long windingsNumber;

    @Column(name = "delta_pxx")
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

    @Column(name = "resist_h")
    private Double resistH;

    @Column(name = "resist_m")
    private Double resistM;

    @Column(name = "resist_l")
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
    private MeteringPoint inputMp;

    @ManyToOne
    @JoinColumn(name = "input_mp_id_h")
    private MeteringPoint inputMpH;

    @ManyToOne
    @JoinColumn(name = "input_mp_id_m")
    private MeteringPoint inputMpM;

    @ManyToOne
    @JoinColumn(name = "input_mp_id_l")
    private MeteringPoint inputMpL;

    @Column(name = "ap_l")
    private Double apL;

    @Column(name = "am_l")
    private Double amL;

    @Column(name = "rp_l")
    private Double rpL;

    @Column(name = "rm_l")
    private Double rmL;

    @Column(name = "ap_m")
    private Double apM;

    @Column(name = "am_m")
    private Double amM;

    @Column(name = "rp_m")
    private Double rpM;

    @Column(name = "rm_m")
    private Double rmM;

    @Column(name = "ap_h")
    private Double apH;

    @Column(name = "am_h")
    private Double amH;

    @Column(name = "rp_h")
    private Double rpH;

    @Column(name = "rm_h")
    private Double rmH;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_out")
    private MeteringPoint meteringPointOut;
}

