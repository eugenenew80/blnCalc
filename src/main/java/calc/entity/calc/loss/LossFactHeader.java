package calc.entity.calc.loss;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import calc.entity.calc.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_loss_fact_headers")
public class LossFactHeader {
    @Id
    private Long id;

    @Column(name = "doc_type_code")
    private String docTypeCode;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private LossFactHeader template;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<LossFactSec1Line> lines1;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<LossFactSec2Line> lines2;

    @ManyToOne
    @JoinColumn(name = "hn_metering_point_id")
    private MeteringPoint hnMeteringPoint;

    @ManyToOne
    @JoinColumn(name = "op_param_id")
    private Parameter opParam;

    @ManyToOne
    @JoinColumn(name = "hn_param_id")
    private Parameter hnParam;
}
