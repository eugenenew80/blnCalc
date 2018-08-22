package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bypass_modes")
@Immutable
public class BypassMode {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bypass_metering_point_id")
    private MeteringPoint bypassMeteringPoint;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "bus_section_reason")
    private String busSectionReason;

    @Column(name = "is_bus_section")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBusSection;

    @OneToMany(mappedBy = "bypassMode", fetch = FetchType.LAZY)
    private List<BypassModeValue> values;
}
