package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_unit_lines_tl")
public class BalanceUnitLineTranslate {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_unit_line_id")
    private BalanceUnitLine line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;

    @Column(name = "short_name")
    private String shortName;
}
