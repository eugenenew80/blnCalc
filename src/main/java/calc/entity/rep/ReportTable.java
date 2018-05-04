package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Immutable
@javax.persistence.Table(name = "calc_report_tables")
public class ReportTable {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "sheet_id")
    private ReportSheet sheet;

    @Column(name = "name")
    private String name;

    @Column(name = "has_header")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean hasHeader;

    @Column(name = "has_footer")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean hasFooter;

    @OneToMany(mappedBy = "table", fetch = FetchType.LAZY)
    private List<ReportDivision> divisions;

    @OneToMany(mappedBy = "table", fetch = FetchType.LAZY)
    private List<ReportAttr> attrs;

}
