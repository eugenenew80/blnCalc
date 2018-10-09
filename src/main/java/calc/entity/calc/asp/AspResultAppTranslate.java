package calc.entity.calc.asp;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_result_apps_tl")
public class AspResultAppTranslate {
    @Id
    @SequenceGenerator(name="calc_asp1_result_apps_tl_s", sequenceName = "calc_asp1_result_apps_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_result_apps_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_result_app_id")
    private AspResultApp app;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "app_name")
    private String appName;
}
