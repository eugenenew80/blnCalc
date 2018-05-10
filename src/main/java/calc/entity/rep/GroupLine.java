package calc.entity.rep;

import calc.entity.calc.MeteringPoint;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "calc_group_lines")
public class GroupLine {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_header_id")
    private GroupHeader groupHeader;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "order_num")
    private Long orderNum;
}
