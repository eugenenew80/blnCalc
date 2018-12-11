package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_voltage_classes")
@Immutable
public class VoltageClass {
    @Id
    private Long id;

    @Column(name = "cachedValue")
    private Long value;
}
