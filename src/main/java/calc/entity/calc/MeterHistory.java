package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
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

    @Column(name = "installed_tt_number")
    private Long ttNumber;

    @Column(name = "tt_mounted_on")
    private String ttMountedOn;

    @ManyToOne
    @JoinColumn(name = "undercount_header_id")
    private UnderCount undercount;

    @ManyToOne
    @JoinColumn(name = "tt_type_id")
    private TtType ttType;

    @ManyToOne
    @JoinColumn(name = "tn_type_id")
    private TnType tnType;

    @Column(name = "tn_direct_inclusion")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTnDirectInclusion;

    @Column(name = "tt_direct_inclusion")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTtDirectInclusion;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private MeterHistory parent;

    @Column(name = "is_tt_installed")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTtInstalled;

    @Column(name = "is_tn_installed")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTnInstalled;
}
