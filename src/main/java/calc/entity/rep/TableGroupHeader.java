package calc.entity.rep;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_table_group_headers")
public class TableGroupHeader {
    @Id
    @SequenceGenerator(name="calc_table_group_headers_s", sequenceName = "calc_table_group_headers_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_table_group_headers_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "division_id")
    private TableDivision division;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private TableSection section;

    @ManyToOne
    @JoinColumn(name = "group_header_id")
    private GroupHeader groupHeader;

    @Column(name = "param_code")
    private String paramCode;
}
