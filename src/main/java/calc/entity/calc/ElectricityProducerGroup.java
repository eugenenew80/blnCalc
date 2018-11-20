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
@Table(name = "calc_electricity_producer_groups")
@Immutable
public class ElectricityProducerGroup {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_partner_id")
    private BusinessPartner bp;

    @OneToMany(mappedBy = "group")
    @MapKey(name = "lang")
    private Map<LangEnum, ElectricityProducerGroupTranslate> translates;
}
