package calc.entity.calc.inter;

import calc.entity.calc.Organization;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_headers")
public class InterHeader {
    @Id
    private Long id;

    @Column(name = "doc_type_code")
    private String docTypeCode;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private InterHeader template;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<InterLine> lines;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<InterNote> notes;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<InterApp> apps;
}
