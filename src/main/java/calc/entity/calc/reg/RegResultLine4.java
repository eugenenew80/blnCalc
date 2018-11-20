package calc.entity.calc.reg;

import calc.entity.calc.Dealer;
import calc.entity.calc.enums.DealTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_reg_result_lines_4")
public class RegResultLine4 {
    @Id
    @SequenceGenerator(name="calc_balance_reg_result_lines_4_s", sequenceName = "calc_balance_reg_result_lines_4_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_reg_result_lines_4_s")
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private RegResultHeader header;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @Column(name="deal_type")
    @Enumerated(EnumType.STRING)
    private DealTypeEnum dealType;

    @Column(name = "val")
    private Double val;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;
}
