package calc.entity.calc.inter;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_apps_tl")
@Immutable
public class InterAppTranslate {
    @Id
    @SequenceGenerator(name="calc_act_inter_pl_apps_tl_s", sequenceName = "calc_act_inter_pl_apps_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_act_inter_pl_apps_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_app_id")
    private InterApp app;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "app_name")
    private String appName;
}
