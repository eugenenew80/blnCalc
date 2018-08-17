package calc.entity.calc;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_headers")
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(
            name = "BalanceSubstResultHeader.calcPeValues",
            procedureName = "calc_calculations.do_pe_values",
            parameters = { @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_bs_result_header_id", type = Long.class) }
    ),

    @NamedStoredProcedureQuery(
            name = "BalanceSubstResultHeader.calcUnbalance",
            procedureName = "calc_calculations.do_unbalance",
            parameters = { @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_bs_result_header_id", type = Long.class) }
    )
})
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
}
