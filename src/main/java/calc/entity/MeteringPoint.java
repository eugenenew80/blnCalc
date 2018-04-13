package calc.entity;

import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;

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

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "metering_point_type_id")
    private Long meteringPointTypeId;
}
