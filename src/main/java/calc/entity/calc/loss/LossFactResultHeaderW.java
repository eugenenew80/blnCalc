package calc.entity.calc.loss;

import calc.entity.calc.enums.BatchStatusEnum;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_loss_fact_result_headers")
@Immutable
public class LossFactResultHeaderW {
    @Id
    private Long id;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;
 }
