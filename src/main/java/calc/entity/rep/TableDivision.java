package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.rep.enums.TablePartEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@javax.persistence.Table(name = "calc_table_divisions")
public class TableDivision {
    @Id
    @SequenceGenerator(name="calc_table_divisions_s", sequenceName = "calc_table_divisions_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_table_divisions_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "sheet_id")
    private ReportSheet sheet;

    @ManyToOne
    @JoinColumn(name =  "table_id")
    private ReportTable table;

    @Column(name = "name")
    private String name;

    @Column(name = "has_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean hasTotal;

    @Column(name = "has_title")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean hasTitle;

    @Column(name = "order_num")
    private Long orderNum;

    @Column(name = "belong_to")
    @Enumerated(EnumType.STRING)
    private TablePartEnum belongTo;

    @OneToMany(mappedBy = "division", fetch = FetchType.LAZY)
    private List<TableSection> sections;

    @OneToMany(mappedBy = "division", fetch = FetchType.LAZY)
    private List<TableRow> rows;
}
