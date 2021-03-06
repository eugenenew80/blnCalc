package calc.entity.calc.bs.ub;

import calc.entity.calc.AccuracyClass;
import calc.entity.calc.Meter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_nb_lines")
public class BalanceSubstResultUbLine {
    @Id
    @SequenceGenerator(name = "calc_bs_result_nb_lines_s", sequenceName = "calc_bs_result_nb_lines_s", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_nb_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_header_id")
    private BalanceSubstResultHeader header;

    @Column(name = "direction")
    private String direction;

    @ManyToOne
    @JoinColumn(name = "mp_id")
    private MeteringPoint meteringPoint;

    @Column(name = "bu_proc")
    private Double buProc;

    @Column(name = "bl_proc")
    private Double blProc;

    @Column(name = "bso_proc")
    private Double bsoProc;

    @Column(name = "bi_proc")
    private Double biProc;

    @Column(name = "b_proc")
    private Double bProc;

    @Column(name = "w")
    private Double w;

    @Column(name = "wa")
    private Double wa;

    @Column(name = "wr")
    private Double wr;

    @Column(name = "dol")
    private Double dol;

    @Column(name = "b2dol2")
    private Double b2dol2;

    @Column(name = "tt_star")
    private String ttStar;

    @Column(name = "ttac_proc")
    private Double ttacProc;

    @Column(name = "i1_nom")
    private Double i1Nom;

    @Column(name = "trab")
    private Double tRab;

    @Column(name = "uavg")
    private Double uavg;

    @Column(name = "i1avg_val")
    private Double i1avgVal;

    @Column(name = "i1avg_proc")
    private Double i1avgProc;

    @Column(name = "btt_factor")
    private Double bttFactor;

    @Column(name = "btt_proc")
    private Double bttProc;

    @ManyToOne
    @JoinColumn(name = "tt_accuracy_class_id")
    private AccuracyClass ttAccuracyClass;

    @ManyToOne
    @JoinColumn(name = "tn_accuracy_class_id")
    private AccuracyClass tnAccuracyClass;

    @ManyToOne
    @JoinColumn(name = "meter_accuracy_class_id")
    private AccuracyClass meterAccuracyClass;

    @ManyToOne
    @JoinColumn(name = "bypass_mp_id")
    private MeteringPoint bypassMeteringPoint;
}
