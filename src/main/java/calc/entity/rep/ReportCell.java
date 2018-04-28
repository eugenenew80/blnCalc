package calc.entity.rep;

import calc.entity.rep.enums.TablePartEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@javax.persistence.Table(name = "calc_table_cells")
public class ReportCell {
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

    @ManyToOne
    @JoinColumn(name = "row_id")
    private ReportRow row;

    @ManyToOne
    @JoinColumn(name = "attr_id")
    private ReportAttr attr;

    @Column
    @Enumerated(EnumType.STRING)
    private TablePartEnum belongTo;

    @Column(name = "val")
    private String val;
}
