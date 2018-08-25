package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_power_transformer_modes")
@Immutable
public class PowerTransformerMode implements Period {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "substation_id")
    private Substation substation;

    @ManyToOne
    @JoinColumn(name = "power_transformer_id")
    private PowerTransformer transformer;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;
}
