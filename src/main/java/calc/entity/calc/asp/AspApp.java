package calc.entity.calc.asp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_apps")
@Immutable
public class AspApp {
    @Id
    @SequenceGenerator(name="calc_asp1_apps_s", sequenceName = "calc_asp1_apps_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_apps_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_header_id")
    private AspHeader header;

    @Column(name = "app_num")
    private Long appNum;

    @OneToMany(mappedBy = "app", fetch = FetchType.LAZY)
    private List<AspAppTranslate> translates;
}
