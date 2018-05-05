package calc.entity.rep;

import calc.entity.rep.enums.AttrTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "calc_table_cells")
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
