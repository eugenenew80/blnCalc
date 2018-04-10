package calc.entity;

import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;

import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@NoArgsConstructor
@Entity
@Table(name = "dict_metering_points")
@Immutable
public class MeteringPoint {
    public MeteringPoint(String code) {
        this.code = code;
    }

    @Id
    private Long id;

    @Column
    private String code;

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "meteringPoint")
    private List<MeteringPointFormula> formulas;
}
