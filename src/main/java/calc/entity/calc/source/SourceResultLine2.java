package calc.entity.calc.source;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.RowTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_source_result_lines")
public class SourceResultLine2 {
    @Id
    @SequenceGenerator(name="calc_balance_source_result_lines_s", sequenceName = "calc_balance_source_result_lines_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_source_result_lines_s")
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private SourceResultLine2 parent;

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

    @Column(name = "row_type_code")
    @Enumerated(EnumType.STRING)
    private RowTypeEnum rowType;

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

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SourceResultLine2Translate> translates;
}
