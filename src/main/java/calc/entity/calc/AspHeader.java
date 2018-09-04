package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp_headers")
public class AspHeader {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bp_id")
    private BusinessPartner businessPartner;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<AspLine> lines;
}
