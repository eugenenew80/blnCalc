package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "media_parameters")
@Immutable
public class Parameter {
    @Id
    private Long id;

    @Column
    private String code;

    @ManyToOne
    @JoinColumn(name="unit_id")
    private Unit unit;

    @Column(name = "is_at")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isAt;

    @Column(name = "is_pt")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isPt;

    @Column(name = "digits_rounding")
    private Integer digitsRounding;
}
