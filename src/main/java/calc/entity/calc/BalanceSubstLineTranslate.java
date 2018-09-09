package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_subst_lines_tl")
@Immutable
public class BalanceSubstLineTranslate {
    @Id
    @SequenceGenerator(name="calc_balance_subst_lines_tl_s", sequenceName = "calc_balance_subst_lines_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_subst_lines_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_subst_line_id")
    private BalanceSubstLine line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;
}
