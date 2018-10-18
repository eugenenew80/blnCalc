package calc.entity.calc.inter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_apps")
@Immutable
public class InterApp {
    @Id
    @SequenceGenerator(name="calc_act_inter_pl_apps_s", sequenceName = "calc_act_inter_pl_apps_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_act_inter_pl_apps_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private InterHeader header;

    @Column(name = "app_num")
    private Long appNum;

    @OneToMany(mappedBy = "app", fetch = FetchType.LAZY)
    private List<InterAppTranslate> translates;
}
