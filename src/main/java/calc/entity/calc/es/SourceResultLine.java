package calc.entity.calc.es;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_source_result_lines")
public class SourceResultLine {
    @Id
    @SequenceGenerator(name="calc_balance_source_result_lines_s", sequenceName = "calc_balance_source_result_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_source_result_lines_s")
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private SourceResultLine parent;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private SourceResultHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column(name = "is_inverse")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse;

    @Column(name = "own_val")
    private Double ownVal;

    @Column(name = "other_val")
    private Double otherVal;

    @Column(name = "total_val")
    private Double totalVal;

    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY)
    private List<SourceResultLineTranslate> translates;
}
