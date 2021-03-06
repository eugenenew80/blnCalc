package calc.entity.rep;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@javax.persistence.Table(name = "calc_table_rows")
public class TableRow {
    @Id
    @SequenceGenerator(name="calc_table_rows_s", sequenceName = "calc_table_rows_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_table_rows_s")
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

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "is_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isTotal;

    @Column(name = "is_include_in_total")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isIncludeInTotal;

    @Column(name = "order_num")
    private Long orderNum;

    @OneToMany(mappedBy = "row", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TableCell> cells;


    @Transient
    public ReportTable getTable() {
        return division.getTable();
    }
}
