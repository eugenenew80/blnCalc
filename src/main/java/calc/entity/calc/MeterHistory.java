package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "mdfem_history")
@Immutable
public class MeterHistory {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "meter_id")
    private Meter meter;

    @Column(name = "counter_factor")
    private Double factor;

    @Column(name = "seal_number")
    private String serial;

    @Column(name = "start_datetime")
    private LocalDate startDateTime;

    @Column(name = "end_datetime")
    private LocalDate endDateTime;
}