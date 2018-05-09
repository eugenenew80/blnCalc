package calc.entity.rep;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "calc_group_headers")
public class GroupHeader {
    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "groupHeader", fetch = FetchType.LAZY)
    private List<GroupLine> lines;
}
