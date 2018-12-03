package calc.entity.calc.svr;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_service_values_reconciliations_l_tl")
@Immutable
public class SvrResultLineTranslate {
    @Id
    @SequenceGenerator(name="calc_service_values_reconciliations_l_tl_s", sequenceName = "calc_service_values_reconciliations_l_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_service_values_reconciliations_l_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_values_reconciliation_l_id")
    private SvrResultLine line;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "name")
    private String name;
}
