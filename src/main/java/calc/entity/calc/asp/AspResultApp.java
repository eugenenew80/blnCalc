package calc.entity.calc.asp;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_result_apps")
public class AspResultApp {
    @Id
    @SequenceGenerator(name="calc_asp1_result_apps_s", sequenceName = "calc_asp1_result_apps_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_result_apps_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_result_header_id")
    private AspResultHeader header;

    @Column(name = "app_num")
    private Long appNum;

    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AspResultAppTranslate> translates;
}
