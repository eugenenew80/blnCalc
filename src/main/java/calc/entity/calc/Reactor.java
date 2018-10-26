package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;
import java.util.Map;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_reactors")
@Immutable
public class Reactor {
    @Id
    private Long id;

    @Column(name = "delta_pr")
    private Double deltaPr;

    @Column(name = "unom")
    private Double unom;

    @ManyToOne
    @JoinColumn(name = "input_mp_id")
    private MeteringPoint inputMp;

    @OneToMany(mappedBy = "reactor")
    @MapKey(name = "lang")
    private Map<LangEnum, ReactorTranslate> translates;
}
