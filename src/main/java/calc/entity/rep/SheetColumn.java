package calc.entity.rep;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@javax.persistence.Table(name = "calc_sheet_columns")
public class SheetColumn {
    @Id
    @SequenceGenerator(name="calc_sheet_columns_s", sequenceName = "calc_sheet_columns_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_sheet_columns_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "sheet_id")
    private ReportSheet sheet;

    @Column(name = "width")
    private Long width;

    @Column(name = "order_num")
    private Long orderNum;

}
