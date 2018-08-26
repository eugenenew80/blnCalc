package calc.entity.calc;

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
    private Double ttStar;

    @Column(name = "ttac_proc")
    private Double ttacProc;

    @Column(name = "i1_nom")
    private Double i1Nom;

    @Column(name = "t_rab")
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
}
