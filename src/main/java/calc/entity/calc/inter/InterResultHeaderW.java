package calc.entity.calc.inter;

import calc.entity.calc.enums.BatchStatusEnum;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Immutable
@Table(name = "calc_act_inter_pl_result_headers")
public class InterResultHeaderW {
    @Id
    private Long id;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;
}

