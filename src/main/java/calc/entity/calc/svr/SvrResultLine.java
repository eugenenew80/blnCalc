package calc.entity.calc.svr;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.DataTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_service_values_reconciliations_l")
public class SvrResultLine {

    @Id
    @SequenceGenerator(name="calc_service_values_reconciliations_l_s", sequenceName = "calc_service_values_reconciliations_l_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_service_values_reconciliations_l_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private SvrResultHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column(name = "cptri_type_code")
    private String typeCode;

    @Column(name = "val")
    private Double val;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name="data_type")
    @Enumerated(EnumType.STRING)
    private DataTypeEnum dataType;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SvrResultLineTranslate> translates;
}
