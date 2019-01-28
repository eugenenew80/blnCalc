package calc.entity.calc.svr;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.DataTypeSupport;
import calc.entity.TemplateLine;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.DataTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_service_values_reconciliations_l")
public class SvrResultLine implements DataTypeSupport {

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

    @Column(name = "is_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTotal;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name="data_type")
    @Enumerated(EnumType.STRING)
    private DataTypeEnum dataType;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SvrResultLineTranslate> translates;
}
