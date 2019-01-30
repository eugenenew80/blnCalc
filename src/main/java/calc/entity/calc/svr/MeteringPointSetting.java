package calc.entity.calc.svr;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.Contract;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.DeterminingMethodEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_metering_point_settings")
@Immutable
public class MeteringPointSetting {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private MeteringPointSettingHeader header;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column(name = "cptri_type_code")
    private String typeCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTotal;

    @Column(name="method_code")
    @Enumerated(EnumType.STRING)
    private DeterminingMethodEnum method;

    @OneToMany(mappedBy = "pointSetting", fetch = FetchType.EAGER)
    private List<MeteringPointSettingTranslate> translates;
}
