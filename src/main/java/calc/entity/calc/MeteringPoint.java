package calc.entity.calc;

import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Immutable;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_metering_points")
@Immutable
public class MeteringPoint {
    @Id
    private Long id;

    @Column
    private String code;

    @Column
    private String name;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "metering_point_type_id")
    private Long meteringPointTypeId;

    @OneToMany(mappedBy = "meteringPoint", fetch = FetchType.LAZY)
    private List<Formula> formulas;

    @OneToMany(mappedBy = "meteringPoint", fetch = FetchType.LAZY)
    private List<MeteringPointParameter> parameters;

    @OneToMany(mappedBy = "meteringPoint", fetch = FetchType.LAZY)
    private List<MeterHistory> meterHistory;

    @OneToMany(mappedBy = "meteringPoint", fetch = FetchType.LAZY)
    private List<UndercountHeader> undercountHeaders;

    @OneToMany(mappedBy = "meteringPoint", fetch = FetchType.LAZY)
    private List<BypassMode> bypassModes;
}
