package calc.entity.rep;

import calc.entity.rep.enums.AttrTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_table_cells")
public class TableCell {
    @Id
    @SequenceGenerator(name="calc_table_cells_s", sequenceName = "calc_table_cells_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_table_cells_s")
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

    @ManyToOne
    @JoinColumn(name = "section_id")
    private TableSection section;

    @ManyToOne
    @JoinColumn(name = "row_id")
    private TableRow row;

    @ManyToOne
    @JoinColumn(name = "attr_id")
    private TableAttr attr;

    @Column(name = "attr_type")
    @Enumerated(EnumType.STRING)
    private AttrTypeEnum attrType;

    @Column(name = "precision")
    private Long precision;

    @Column(name = "val")
    private String val;

    @Column(name = "formula")
    private String formula;
}
