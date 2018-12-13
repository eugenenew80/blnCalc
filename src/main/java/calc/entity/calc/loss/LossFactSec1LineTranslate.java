package calc.entity.calc.loss;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_loss_fact_sec1_lines_tl")
@Immutable
public class LossFactSec1LineTranslate {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "line_id")
    private LossFactSec1Line line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;
}
