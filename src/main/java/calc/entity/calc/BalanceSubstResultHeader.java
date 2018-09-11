package calc.entity.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_headers")
public class BalanceSubstResultHeader {
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

    @Column(name = "nbd_val")
    private Double nbdVal;

    @Column(name = "nbd_proc")
    private Double nbdProc;

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

    @Column(name = "section1_total")
    private Double total1;

    @Column(name = "section2_total")
    private Double total2;

    @Column(name = "section3_total")
    private Double total3;

    @Column(name = "section4_total")
    private Double total4;
}
