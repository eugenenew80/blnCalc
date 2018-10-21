package calc.entity.calc.seg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_result_apps")
public class SegResultApp {
    @Id
    @SequenceGenerator(name="calc_balance_segment_result_apps_s", sequenceName = "calc_balance_segment_result_apps_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_result_apps_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private SegResultHeader header;

    @Column(name = "app_num")
    private Long appNum;

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SegResultAppTranslate> translates;
}
