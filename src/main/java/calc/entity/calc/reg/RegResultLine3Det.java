package calc.entity.calc.reg;

import calc.entity.calc.EnergySource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_reg_result_lines_3_det")
public class RegResultLine3Det {
    @Id
    @SequenceGenerator(name="calc_balance_reg_result_lines_3_det_s", sequenceName = "calc_balance_reg_result_lines_3_det_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_reg_result_lines_3_det_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_line_id")
    private RegResultLine3 line;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private RegResultHeader header;

    @ManyToOne
    @JoinColumn(name = "energy_source_id")
    private EnergySource energySource;

    @Column(name = "own_val")
    private Double ownVal;

    @Column(name = "other_val")
    private Double otherVal;

    @Column(name = "total_val")
    private Double totalVal;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;
}
