package calc.entity.calc.seg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_apps")
@Immutable
public class SegApp {
    @Id
    @SequenceGenerator(name="calc_balance_segment_apps_s", sequenceName = "calc_balance_segment_apps_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_apps_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private SegHeader header;

    @Column(name = "app_num")
    private Long appNum;

    @OneToMany(mappedBy = "app", fetch = FetchType.LAZY)
    private List<SegAppTranslate> translates;
}
