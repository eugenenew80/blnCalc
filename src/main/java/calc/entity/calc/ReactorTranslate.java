package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_reactors_tl")
@Immutable
public class ReactorTranslate {
    @Id
    private Long id;

    @Column(name = "lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "reactor_id")
    private Reactor reactor;
}
