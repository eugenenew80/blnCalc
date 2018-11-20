package calc.entity.calc.es;

import calc.entity.calc.*;
import calc.entity.calc.Parameter;
import calc.entity.calc.reg.RegLine1;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;


@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_source_headers")
public class SourceHeader {
    @Id
    private Long id;

    @Column(name = "doc_type_code")
    private String docTypeCode;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private SourceHeader template;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private ElectricityProducerGroup group;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    private Formula formula;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<SourceLine> lines;
}
