package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "mdfem_tn_types")
@Immutable
public class TnType {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "accuracy_class_id")
    private AccuracyClass accuracyClass;

    @Column(name = "rated_voltage1")
    private Double ratedCurrent1;

    @Column(name = "rated_voltage2")
    private Double ratedCurrent2;
}
