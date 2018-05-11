package calc.entity.rep;

import calc.entity.rep.enums.AttrTypeEnum;
import calc.entity.rep.enums.ValueTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_table_attrs")
public class TableAttr {
    @Id
    @SequenceGenerator(name="calc_table_attrs_s", sequenceName = "calc_table_attrs_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_table_attrs_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "row_template_id")
    private RowTemplate rowTemplate;

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
}
