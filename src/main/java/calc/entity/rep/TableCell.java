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

    public static TableCell fromAttr(TableRow row, TableAttr attr) {
        TableCell cell = new TableCell();
        cell.setReport(row.getReport());
        cell.setRow(row);
        cell.setAttr(attr);
        return cell;
    }
}
