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
@Table(name = "calc_asp1_result_headers")
public class AspResultHeader {

    @Id
    @SequenceGenerator(name="calc_asp1_result_headers_s", sequenceName = "calc_asp1_result_headers_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_result_headers_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_header_id")
    private AspHeader header;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name="period_type")
    @Enumerated(EnumType.STRING)
    private PeriodTypeEnum periodType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;

    @Column(name = "state_code")
    private String state;
}