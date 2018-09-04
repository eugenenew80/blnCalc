package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp_lines")
public class AspLine {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp_header_id")
    private AspHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "item_num")
    private Long itemNum;

    @Column(name = "name")
    private String name;
}
