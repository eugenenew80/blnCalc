package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_dealers_tl")
@Immutable
public class DealerTranslate {
    @Id
    private Long id;

    @Column(name = "lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;
}
