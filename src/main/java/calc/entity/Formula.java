package calc.entity;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_formulas")
@NamedEntityGraph(name="Formula.allJoins", attributeNodes = {
    @NamedAttributeNode("meteringPoint"),
    @NamedAttributeNode("parameter"),
    @NamedAttributeNode("unit")
})
public class Formula {
    @Id
    private Long id;

    @Column
    private String code;

    @Column
    private String description;

    @Column
    private String text;

    @Column(name = "param_type")
    private String paramType;

    @ManyToOne
    @JoinColumn(name="metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name="parameter_id")
    private Parameter parameter;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_multi_values")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean multiValues;
}
