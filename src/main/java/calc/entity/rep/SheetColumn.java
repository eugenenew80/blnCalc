package calc.entity.rep;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Immutable
@javax.persistence.Table(name = "calc_sheet_columns")
public class SheetColumn {
    @Id
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
