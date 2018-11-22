package calc.entity.calc.source;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.RowTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_source_lines")
@Immutable
public class SourceLine2 {
    @Id
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private SourceHeader header;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private SourceLine2 parent;

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

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY)
    private List<SourceLine2Translate> translates;
}
