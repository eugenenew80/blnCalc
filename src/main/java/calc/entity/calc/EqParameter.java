package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"code"})
@Entity
@Table(name = "calc_equipment_param_types")
@Immutable
public class EqParameter {
    @Column
    private String code;
}
