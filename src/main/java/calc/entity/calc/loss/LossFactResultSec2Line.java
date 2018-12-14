package calc.entity.calc.loss;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_loss_fact_result_sec2_lines")
public class LossFactResultSec2Line {

    @Id
    @SequenceGenerator(name="calc_loss_fact_result_sec2_lines_s", sequenceName = "calc_loss_fact_result_sec2_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_loss_fact_result_sec2_lines_s")
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private LossFactResultHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization org;

    @Column(name = "is_inverse")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse;

    @Column(name = "ap")
    private Double ap;

    @Column(name = "am")
    private Double am;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "last_update_by")
    private Long lastUpdateBy;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LossFactResultSec2LineTranslate> translates;
}
