package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_source_type_priorities")
@NamedEntityGraph(name="SourceTypePriority.allJoins", attributeNodes = {
    @NamedAttributeNode("meteringPoint"),
    @NamedAttributeNode("sourceType")
})
public class SourceTypePriority {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name="metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "source_type_id")
    private SourceType sourceType;

    @Column
    private Byte priority;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}
