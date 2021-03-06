package calc.entity.calc.bs;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.DocHeader;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Organization;
import calc.entity.calc.Substation;
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
@Table(name = "calc_bs_result_headers")
public class BalanceSubstResultHeader  implements DocHeader {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_subst_header_id")
    private BalanceSubstHeader header;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "substation_id")
    private Substation substation;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;

    @Column(name="period_type")
    @Enumerated(EnumType.STRING)
    private PeriodTypeEnum periodType;

    @Column(name="data_type_code")
    @Enumerated(EnumType.STRING)
    private DataTypeEnum dataType;

    @Column(name = "nbd_val")
    private Double nbdVal;

    @Column(name = "nbd_proc")
    private Double nbdProc;

    @Column(name = "nbf_val")
    private Double nbfVal;

    @Column(name = "nbf_proc")
    private Double nbfProc;

    @Column(name = "nbdiff_val")
    private Double nbDifVal;

    @Column(name = "nbdiff_proc")
    private Double nbDifProc;

    @ManyToOne
    @JoinColumn(name = "section1_metering_point_id")
    private MeteringPoint meteringPoint1;

    @ManyToOne
    @JoinColumn(name = "section2_metering_point_id")
    private MeteringPoint meteringPoint2;

    @ManyToOne
    @JoinColumn(name = "section3_metering_point_id")
    private MeteringPoint meteringPoint3;

    @ManyToOne
    @JoinColumn(name = "section4_metering_point_id")
    private MeteringPoint meteringPoint4;

    @ManyToOne
    @JoinColumn(name = "loss_fact_metering_point_id")
    private MeteringPoint lossFactMeteringPoint;

    @Column(name = "section1_total")
    private Double total1;

    @Column(name = "section2_total")
    private Double total2;

    @Column(name = "section3_total")
    private Double total3;

    @Column(name = "section4_total")
    private Double total4;

    @Column(name = "section5_total")
    private Double total5;

    @Column(name = "section6_total")
    private Double total6;

    @Column(name = "loss_fact")
    private Double lossFact;

    @Column(name = "is_active")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isActive;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "last_update_by")
    private Long lastUpdateBy;
}
