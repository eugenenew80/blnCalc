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

    private Double bsoProc;

    private Double biProc;

    private Double bProc;

    private Double w;

    private Double wa;

    private Double wr;

    private Double doi;

    private Double b2doi2;

    private Double ttStar;

    private Double ttacProc;

    private Double i1Nom;

    private Double tRab;

    private Double uavg;

    private Double i1avgVal;

    private Double i1avgProc;

    private Double bttFactor;

    private Double bttProc;
}
