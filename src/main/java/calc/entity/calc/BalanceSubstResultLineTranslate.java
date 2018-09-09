package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_lines_tl")
public class BalanceSubstResultLineTranslate {
    @Id
    @SequenceGenerator(name="calc_bs_result_lines_tl_s", sequenceName = "calc_bs_result_lines_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_lines_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_line_id")
    private BalanceSubstResultLine line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;
}
