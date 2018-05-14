package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@javax.persistence.Table(name = "calc_table_sections")
public class TableSection {
    @Id
    @SequenceGenerator(name="calc_table_sections_s", sequenceName = "calc_table_sections_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_table_sections_s")
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

    @Column(name = "code")
    private String code;

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
