package calc.entity.calc.asp;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_apps_tl")
@Immutable
public class AspAppTranslate {
    @Id
    @SequenceGenerator(name="calc_asp1_apps_tl_s", sequenceName = "calc_asp1_apps_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_apps_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_app_id")
    private AspApp app;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "app_name")
    private String appName;
}
