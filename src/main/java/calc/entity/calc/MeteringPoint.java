package calc.entity.calc;

import calc.entity.calc.enums.PointTypeEnum;
import lombok.*;
import javax.persistence.*;

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

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "metering_point_type_id")
    private Long meteringPointTypeId;

    @ManyToOne
    @JoinColumn(name = "rated_voltage")
    private VoltageClass voltageClass;

    @OneToMany(mappedBy = "meteringPoint", fetch = FetchType.LAZY)
    private List<Formula> formulas;

    @OneToMany(mappedBy = "meteringPoint", fetch = FetchType.LAZY)
    private List<MeteringPointParameter> parameters;

    @OneToMany(mappedBy = "meteringPoint", fetch = FetchType.LAZY)
    private List<MeterHistory> meterHistory;

    @Transient
    public PointTypeEnum getPointType() {
        if (meteringPointTypeId == null || meteringPointTypeId > 2) return null;
        return PointTypeEnum.values()[meteringPointTypeId.intValue()-1];
    }
}
