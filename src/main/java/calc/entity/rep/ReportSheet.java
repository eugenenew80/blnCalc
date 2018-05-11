package calc.entity.rep;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import javax.persistence.Table;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_report_sheets")
public class ReportSheet {
    @Id
    @SequenceGenerator(name="calc_report_sheets_s", sequenceName = "calc_report_sheets_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_report_sheets_s")

    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @Column(name = "name")
    private String name;

    @Column(name = "row_count")
    private Long rowCount;

    @Column(name = "column_count")
    private Long columnCount;

    @Column(name = "order_num")
    private Long orderNum;

    @OneToMany(mappedBy = "sheet", fetch = FetchType.LAZY)
    private List<ReportTable> tables;

    @OneToMany(mappedBy = "sheet", fetch = FetchType.LAZY)
    private List<SheetColumn> columns;
}
