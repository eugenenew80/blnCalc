package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_metering_point_settings")
@Immutable
public class MeteringPointSetting {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private ContractKeg contract;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "cptri_type_code")
    private String typeCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTotal;
}
