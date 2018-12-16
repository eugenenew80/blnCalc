package calc.entity.calc.distr;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.BalanceUnit;
import calc.entity.calc.Organization;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_distribution_result_h")
@Immutable
public class DistrResultHeader {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization org;

    @ManyToOne
    @JoinColumn(name = "balance_unit_id")
    private BalanceUnit balanceUnit;

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

    @Column(name = "is_active")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isActive;

    @Column(name = "version")
    private Long version;

    @Column(name="calc_type")
    @Enumerated(EnumType.STRING)
    private DataTypeEnum dataType;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<DistrResultLineTop> lines;
}
