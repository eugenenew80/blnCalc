package calc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_values")
public class Value {
    @Id
    private Long id;

    @Column(name = "metering_point_id")
    private Long meteringPointId;

    @Column(name = "formula_id")
    private Long formulaId;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column
    private Double val;
}
