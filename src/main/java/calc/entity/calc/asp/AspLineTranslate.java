package calc.entity.calc.asp;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_lines_tl")
@Immutable
public class AspLineTranslate {
    @Id
    @SequenceGenerator(name="calc_asp1_lines_tl_s", sequenceName = "calc_asp1_lines_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_lines_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_line_id")
    private AspLine line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;
}
