package calc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
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

    @OneToMany(mappedBy = "formula")
    @Fetch(FetchMode.SUBSELECT)
    private List<FormulaParameter> parameters;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}
