package calc.entity.calc;

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
