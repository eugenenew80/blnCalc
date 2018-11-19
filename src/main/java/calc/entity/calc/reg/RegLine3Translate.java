package calc.entity.calc.reg;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_reg_lines_3_tl")
@Immutable
public class RegLine3Translate {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "line_id")
    private RegLine3 line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;
}
