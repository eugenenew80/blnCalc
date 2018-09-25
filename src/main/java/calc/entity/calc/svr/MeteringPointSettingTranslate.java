package calc.entity.calc.svr;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_metering_point_settings_tl \n")
@Immutable
public class MeteringPointSettingTranslate {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "metering_point_setting_id")
    private MeteringPointSetting pointSetting;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;
}
