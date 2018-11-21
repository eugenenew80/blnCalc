package calc.entity.calc.source;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_source_result_lines_f")
public class SourceResultLine1 {
    @Id
    @SequenceGenerator(name="calc_balance_source_result_lines_f_s", sequenceName = "calc_balance_source_result_lines_f_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_source_result_lines_f_s")
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private SourceResultHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

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
