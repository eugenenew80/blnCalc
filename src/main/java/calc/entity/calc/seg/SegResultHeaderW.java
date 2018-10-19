package calc.entity.calc.seg;

import calc.entity.calc.enums.BatchStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_result_headers")
public class SegResultHeaderW {

    @Id
    private Long id;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;
}
