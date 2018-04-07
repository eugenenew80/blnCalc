package calc.entity;

import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_power_transformers")
@Immutable
public class PowerTransformer {
    @Id
    private Long id;

    @Column(name = "snom")
    private Double snom;

    @Column(name = "delta_pxx")
    private Double deltaPxx;

    @Column(name = "unom_h")
    private Double unomH;

    @Column(name = "pkz_hm")
    private Double pkzHM;

    @Column(name = "pkz_hl")
    private Double pkzHL;

    @Column(name = "pkz_ml")
    private Double pkzML;
}
