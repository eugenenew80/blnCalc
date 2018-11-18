package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.Map;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_dealers")
@Immutable
public class Dealer {
    @Id
    private Long id;

    @OneToMany(mappedBy = "dealer")
    @MapKey(name = "lang")
    private Map<LangEnum, DealerTranslate> translates;
}
