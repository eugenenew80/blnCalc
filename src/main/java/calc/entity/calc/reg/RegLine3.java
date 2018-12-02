package calc.entity.calc.reg;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.TemplateLine;
import calc.entity.calc.BalanceUnit;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_reg_lines_3")
@Immutable
public class RegLine3 implements TemplateLine {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private RegLine3 parent;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private RegHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column(name = "is_inverse")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "balance_unit_id")
    private BalanceUnit balanceUnit;

    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY)
    private List<RegLine3Translate> translates;

    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY)
    private List<RegLine3Det> details;
}
