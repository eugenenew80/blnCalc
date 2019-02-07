package calc.entity.calc.loss;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.DocHeader;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import calc.entity.calc.Parameter;
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
@Table(name = "calc_loss_fact_result_headers")
public class LossFactResultHeader  implements DocHeader {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loss_fact_header_id")
    private LossFactHeader header;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name="period_type")
    @Enumerated(EnumType.STRING)
    private PeriodTypeEnum periodType;

    @Column(name="data_type")
    @Enumerated(EnumType.STRING)
    private DataTypeEnum dataType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;

    @Column(name = "state_code")
    private String state;

    @Column(name = "is_active")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isActive;

    @Column(name = "ap")
    private Double ap;

    @Column(name = "am")
    private Double am;

    @Column(name = "r32")
    private Double r32;

    @Column(name = "r321")
    private Double r321;

    @Column(name = "r322")
    private Double r322;

    @Column(name = "r323")
    private Double r323;

    @Column(name = "r324")
    private Double r324;

    @Column(name = "r325")
    private Double r325;

    @Column(name = "r326")
    private Double r326;

    @Column(name = "loss_fact")
    private Double lossFact;

    @ManyToOne
    @JoinColumn(name = "hn_metering_point_id")
    private MeteringPoint hnMeteringPoint;

    @ManyToOne
    @JoinColumn(name = "op_param_id")
    private calc.entity.calc.Parameter opParam;

    @ManyToOne
    @JoinColumn(name = "hn_param_id")
    private Parameter hnParam;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "last_update_by")
    private Long lastUpdateBy;
}
