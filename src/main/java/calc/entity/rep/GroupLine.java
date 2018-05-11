package calc.entity.rep;

import calc.entity.calc.MeteringPoint;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_group_lines")
public class GroupLine {
    @Id
    @SequenceGenerator(name="calc_group_lines_s", sequenceName = "calc_group_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_group_lines_s")
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
