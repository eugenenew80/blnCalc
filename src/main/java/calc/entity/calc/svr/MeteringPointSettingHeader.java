package calc.entity.calc.svr;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.*;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.DeterminingMethodEnum;
import calc.entity.calc.source.SourceLine1;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_metering_point_setting_headers")
@Immutable
public class MeteringPointSettingHeader {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private BusinessPartner customer;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization org;

    @Column(name = "is_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTotal;

    @Column(name = "is_central")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isCentral;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<MeteringPointSetting> lines;
}
