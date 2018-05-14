package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import javax.persistence.Table;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_reports")
public class Report {
    @Id
    @SequenceGenerator(name="calc_reports_s", sequenceName = "calc_reports_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_reports_s")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "report_type")
    private String reportType;

    @Column(name= "is_template")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTemplate;

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY)
    private List<ReportSheet> sheets;

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY)
    private List<ReportTable> tables;

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY)
    private List<TableDivision> divisions;

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY)
    private List<TableSection> sections;

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY)
    private List<TableCell> cells;
}
