package calc.entity.rep;

import calc.entity.rep.enums.AttrTypeEnum;
import calc.entity.rep.enums.TablePartEnum;
import calc.entity.rep.enums.ValueTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "calc_table_attrs")
public class ReportAttr {
    @Id
    private Long id;
    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "sheet_id")
    private ReportSheet sheet;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private ReportTable table;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "attr_type")
    @Enumerated(EnumType.STRING)
    private AttrTypeEnum attrType;

    @Column(name = "precision")
    private Long precision;

    @Column(name = "value_type")
    @Enumerated(EnumType.STRING)
    private ValueTypeEnum valueType;

    @Column(name = "order_num")
    private Long orderNum;

    @Column(name = "belong_to")
    @Enumerated(EnumType.STRING)
    private TablePartEnum belongTo;
}
