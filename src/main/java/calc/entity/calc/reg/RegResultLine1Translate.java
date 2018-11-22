package calc.entity.calc.reg;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_reg_result_lines_1")
public class RegResultLine1Translate {
    @Id
    @SequenceGenerator(name="calc_balance_reg_result_lines_1_s", sequenceName = "calc_balance_reg_result_lines_1_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_reg_result_lines_1_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_line_id")
    private RegResultLine1 line;

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
