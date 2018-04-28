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

    @OneToMany(mappedBy = "sheet", fetch = FetchType.LAZY)
    private List<ReportTable> tables;
}
