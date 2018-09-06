package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_service_values_reconciliations")
public class SvrHeader {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private ContractKeg contract;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name = "state")
    private String state;

    @Column(name = "val")
    private Double val;

    @Column(name = "transfer_to_fin_date")
    private LocalDateTime transferToErpDate;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;

    @Column(name="period_type")
    @Enumerated(EnumType.STRING)
    private PeriodTypeEnum periodType;

    @Column(name = "period_start_date")
    private LocalDate startDate;

    @Column(name = "period_end_date")
    private LocalDate endDate;

    @Column(name = "is_current_record")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isCurrentRecord;

    @ManyToOne
    @JoinColumn(name = "total_metering_point_id")
    private MeteringPoint totalMeteringPoint;
}
