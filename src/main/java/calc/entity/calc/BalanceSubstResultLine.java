package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_lines")
public class BalanceSubstResultLine {
    @Id
    @SequenceGenerator(name="calc_bs_result_lines_s", sequenceName = "calc_bs_result_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_header_id")
    private BalanceSubstResultHeader header;

    @Column(name = "section")
    private String section;

    @Column(name = "sub_section")
    private String subSection;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "val")
    private Double val;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BalanceSubstResultLineTranslate> translates;
}
