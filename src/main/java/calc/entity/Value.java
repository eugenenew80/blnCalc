package calc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_values")
public class Value {
    @Id
    @SequenceGenerator(name="calc_values_s", sequenceName = "calc_values_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_values_s")
    private Long id;

    @Column(name = "metering_point_id")
    private Long meteringPointId;

    @Column(name = "formula_id")
    private Long formulaId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column
    private Double val;
}
