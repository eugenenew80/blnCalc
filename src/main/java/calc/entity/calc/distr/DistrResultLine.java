package calc.entity.calc.distr;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.ElectricityProducerGroup;
import calc.entity.calc.EnergySource;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_distribution_result_l")
@Immutable
public class DistrResultLine {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private DistrResultHeader header;

    @ManyToOne
    @JoinColumn(name = "top_line_id")
    private DistrResultLineTop topLine;

    @ManyToOne
    @JoinColumn(name = "energy_group_id")
    private ElectricityProducerGroup electricityGroup;

    @Column(name = "plan_val")
    private Double planVal;

    @Column(name = "own_val")
    private Double ownVal;

    @Column(name = "other_val")
    private Double otherVal;

    @Column(name = "total_val")
    private Double totalVal;

    @Column(name = "is_manual")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isManual;

    @Column(name = "is_frozen")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isFrozen;

    @Column(name = "freezing_reason")
    private String freezingReason;
}
