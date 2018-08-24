package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime startDate;

    @Column(name = "end_datetime")
    private LocalDateTime endDate;

    @Column(name = "old_metering_reading_ai")
    private Double apPrev;

    @Column(name = "old_metering_reading_ae")
    private Double amPrev;

    @Column(name = "old_metering_reading_ri")
    private Double rpPrev;

    @Column(name = "old_metering_reading_re")
    private Double rmPrev;

    @Column(name = "new_metering_reading_ai")
    private Double apNew;

    @Column(name = "new_metering_reading_ae")
    private Double amNew;

    @Column(name = "new_metering_reading_ri")
    private Double rpNew;

    @Column(name = "new_metering_reading_re")
    private Double rmNew;

    @ManyToOne
    @JoinColumn(name = "undercount_header_id")
    private Undercount undercount;

    @ManyToOne
    @JoinColumn(name = "tt_type_id")
    private TtType ttType;
}
