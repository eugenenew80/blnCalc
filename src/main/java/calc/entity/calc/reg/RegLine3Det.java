package calc.entity.calc.reg;

import calc.entity.calc.ElectricityProducerGroup;
import calc.entity.calc.EnergySource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_reg_lines_3_det")
@Immutable
public class RegLine3Det {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private RegHeader header;

    @ManyToOne
    @JoinColumn(name = "line_id")
    private RegLine3 line;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "energy_group_id")
    private ElectricityProducerGroup electricityGroup;
}
