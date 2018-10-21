package calc.entity.calc;

import calc.entity.calc.enums.BalanceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_units")
@Immutable
public class BalanceUnit {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private BalanceUnit parent;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name="balance_type_code")
    @Enumerated(EnumType.STRING)
    private BalanceTypeEnum balanceType;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY)
    private List<BalanceUnitLine> lines;
}
