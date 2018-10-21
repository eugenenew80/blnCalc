package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.seg.SegResultLine;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_unit_lines_tl")
public class BalanceUnitLineTranslate {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_unit_line_id")
    private SegResultLine line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;

    @Column(name = "short_name")
    private String shortName;
}
