package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "mdfem_undercount_header")
@Immutable
public class UndercountHeader {

    @Id
    private Long id;

    @Column(name = "doc_number")
    private String docNum;

    @Column(name = "doc_date")
    private LocalDate docDate;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "violation_datetime")
    private LocalDateTime violationDateTime;

    @Column(name = "restoration_datetime")
    private LocalDateTime restorationDateTime;

    @Column(name = "loss_amount")
    private Double val;

    @Column(name = "is_ignore_meter_reading")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isIgnoreMeteringReading;

    @Column(name = "is_active")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "parameter_type_id")
    private Parameter parameter;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization org;
}
