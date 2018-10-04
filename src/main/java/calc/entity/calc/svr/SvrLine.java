package calc.entity.calc.svr;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_service_values_reconciliations_l")
public class SvrLine {

    @Id
    @SequenceGenerator(name="calc_service_values_reconciliations_l_s", sequenceName = "calc_service_values_reconciliations_l_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_service_values_reconciliations_l_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private SvrHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "cptri_type_code")
    private String typeCode;

    @Column(name = "val")
    private Double val;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SvrLineTranslate> translates;
}
