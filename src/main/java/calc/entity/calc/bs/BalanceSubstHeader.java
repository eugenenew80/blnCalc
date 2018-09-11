package calc.entity.calc.bs;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import calc.entity.calc.Substation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_subst_headers")
public class BalanceSubstHeader {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "substation_id")
    private Substation substation;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "section1_metering_point_id")
    private MeteringPoint meteringPoint1;

    @ManyToOne
    @JoinColumn(name = "section2_metering_point_id")
    private MeteringPoint meteringPoint2;

    @ManyToOne
    @JoinColumn(name = "section3_metering_point_id")
    private MeteringPoint meteringPoint3;

    @ManyToOne
    @JoinColumn(name = "section4_metering_point_id")
    private MeteringPoint meteringPoint4;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<BalanceSubstLine> lines;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<BalanceSubstMrLine> mrLines;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<BalanceSubstUbLine> ubLines;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<BalanceSubstULine> uLines;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<BalanceSubstPeLine> peLines;
}
