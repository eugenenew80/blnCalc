package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Immutable
@javax.persistence.Table(name = "calc_table_sections")
public class TableSection {
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
    private TableDivision division;

    @Column(name = "name")
    private String name;

    @Column(name = "has_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean hasTotal;

    @Column(name = "has_title")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean hasTitle;

    @Column(name = "order_num")
    private Long orderNum;

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<TableRow> rows;
}
