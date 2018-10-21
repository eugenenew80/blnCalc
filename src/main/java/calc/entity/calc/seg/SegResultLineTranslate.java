package calc.entity.calc.seg;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_result_lines_tl")
public class SegResultLineTranslate {
    @Id
    @SequenceGenerator(name="calc_balance_segment_result_lines_tl_s", sequenceName = "calc_balance_segment_result_lines_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_result_lines_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_line_id")
    private SegResultLine line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;
}
