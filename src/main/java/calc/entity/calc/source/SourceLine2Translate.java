package calc.entity.calc.source;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_source_lines_tl")
@Immutable
public class SourceLine2Translate {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "line_id")
    private SourceLine2 line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;
}
