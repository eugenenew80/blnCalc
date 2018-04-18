package calc.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_formula_params")
public class MeteringPointParameter {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name="metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name="parameter_id")
    private Parameter parameter;
}
