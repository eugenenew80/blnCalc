package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bypass_modes")
@Immutable
public class BypassModeValue {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bypass_mode_id")
    private BypassMode bypassMode;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter parameter;

    @Column(name = "start_value")
    private Double startValue;

    @Column(name = "end_value")
    private Double endValue;

    @Column(name = "factor")
    private Double factor;
}
