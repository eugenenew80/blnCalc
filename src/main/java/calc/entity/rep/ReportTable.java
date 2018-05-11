package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_report_tables")
public class ReportTable {
    @Id
    @SequenceGenerator(name="calc_report_tables_s", sequenceName = "calc_report_tables_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_report_tables_s")
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

    @Column(name = "order_num")
    private Long orderNum;

    @OneToMany(mappedBy = "table", fetch = FetchType.LAZY)
    private List<TableDivision> divisions;

    @ManyToOne
    @JoinColumn(name = "body_row_template_id")
    private RowTemplate bodyRowTemplate;

    @ManyToOne
    @JoinColumn(name = "body_total_row_template_id")
    private RowTemplate bodyTotalTemplate;

    @ManyToOne
    @JoinColumn(name = "header_row_template_id")
    private RowTemplate headerRowTemplate;

    @ManyToOne
    @JoinColumn(name = "footer_row_template_id")
    private RowTemplate footerRowTemplate;
}
