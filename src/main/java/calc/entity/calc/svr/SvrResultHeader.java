package calc.entity.calc.svr;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.DocHeader;
import calc.entity.calc.Contract;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
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
public class SvrResultHeader implements DocHeader {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

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

    @Column(name = "is_active")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isActive;

    @Column(name="data_type")
    @Enumerated(EnumType.STRING)
    private DataTypeEnum dataType;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "last_update_by")
    private Long lastUpdateBy;
}
