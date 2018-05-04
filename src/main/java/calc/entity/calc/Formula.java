package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Immutable
@Table(name = "calc_formulas")
@NamedEntityGraph(name="Formula.allJoins", attributeNodes = {
    @NamedAttributeNode("meteringPoint")
})
public class Formula {
    @Id
    private Long id;

    @Column
    private String code;

    @Column
    private String description;

    @Column
    private String text;

    @ManyToOne
    @JoinColumn(name="metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}
