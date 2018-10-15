package calc.entity.calc.bs;

import calc.entity.calc.enums.BatchStatusEnum;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_headers")
@Immutable
public class BalanceSubstResultHeaderW {
    @Id
    private Long id;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;
}
