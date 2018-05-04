package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.persistence.Table;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "calc_reports")
public class Report {
    @Id
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
    private List<ReportCell> cells;
}
