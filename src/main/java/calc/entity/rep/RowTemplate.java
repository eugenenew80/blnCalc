package calc.entity.rep;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_row_templates")
public class RowTemplate {
    @Id
    @SequenceGenerator(name="calc_row_templates_s", sequenceName = "calc_row_templates_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_row_templates_s")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "rowTemplate", fetch = FetchType.LAZY)
    private List<TableAttr> attrs;
}
