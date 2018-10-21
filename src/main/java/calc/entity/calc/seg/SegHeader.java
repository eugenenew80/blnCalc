package calc.entity.calc.seg;

import calc.entity.calc.BalanceUnit;
import calc.entity.calc.Organization;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_headers")
public class SegHeader {
    @Id
    private Long id;

    @Column(name = "doc_type_code")
    private String docTypeCode;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private SegHeader template;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "balance_unit_id")
    private BalanceUnit balanceUnit;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<SegNote> notes;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<SegApp> apps;
}
