package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "mdfem_eem_types")
@Immutable
public class EemType {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "accuracy_class_id")
    private AccuracyClass accuracyClass;

    @Column(name = "rated_current")
    private Double ratedCurrent;

    @Column(name = "rated_voltage")
    private Double ratedVoltage;
}
