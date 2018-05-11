package calc.entity.rep;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "calc_group_headers")
public class GroupHeader {
    @Id
    @SequenceGenerator(name="calc_group_headers_s", sequenceName = "calc_group_headers_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_group_headers_s")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "groupHeader", fetch = FetchType.LAZY)
    private List<GroupLine> lines;
}
