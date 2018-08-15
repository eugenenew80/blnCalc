package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_result_u_lines")
public class BalanceSubstResultULine {
    @Id
    @SequenceGenerator(name = "calc_balance_result_u_lines_s", sequenceName = "calc_balance_result_u_lines_s", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_result_u_lines_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_header_id")
    private BalanceSubstResultHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "ti_num")
    private Long tiNum;

    @Column(name = "ti_name")
    private String tiName;

    @Column(name = "amount")
    private Double val;
}
