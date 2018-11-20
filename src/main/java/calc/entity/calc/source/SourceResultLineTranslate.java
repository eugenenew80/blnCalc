package calc.entity.calc.source;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_source_result_lines_tl")
public class SourceResultLineTranslate {
    @Id
    @SequenceGenerator(name="calc_balance_source_result_lines_tl_s", sequenceName = "calc_balance_source_result_lines_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_source_result_lines_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_line_id")
    private SourceResultLine line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;
}
