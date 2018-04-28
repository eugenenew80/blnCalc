package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.rep.enums.TablePartEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@javax.persistence.Table(name = "calc_table_rows")
public class ReportRow {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "sheet_id")
    private ReportSheet sheet;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private ReportTable table;

    @ManyToOne
    @JoinColumn(name = "division_id")
    private ReportDivision division;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private ReportSection section;

    @Column(name = "name")
    private String name;

    @Column(name = "is_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTotal;

    @Column(name = "order_num")
    private Long orderNum;

    @Column(name = "belong_to")
    @Enumerated(EnumType.STRING)
    private TablePartEnum belongTo;

    @OneToMany(mappedBy = "row", fetch = FetchType.LAZY)
    private List<ReportCell> cells;
}
