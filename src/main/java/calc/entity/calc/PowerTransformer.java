package calc.entity.calc;

import calc.entity.calc.enums.TransformerTypeEnum;
import calc.entity.calc.enums.LangEnum;
import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;
import java.util.Map;

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

    @ManyToOne
    @JoinColumn(name = "input_mp_id")
    private MeteringPoint inputMp;

    @ManyToOne
    @JoinColumn(name = "input_hl_id")
    private MeteringPoint inputMpH;

    @ManyToOne
    @JoinColumn(name = "input_ml_id")
    private MeteringPoint inputMpM;

    @ManyToOne
    @JoinColumn(name = "input_ll_id")
    private MeteringPoint inputMpL;

    @Column(name = "windings_number")
    private Long windingsNumber;

    @Column(name="equipment_type")
    @Enumerated(EnumType.STRING)
    private TransformerTypeEnum transformerType;

    @Column(name = "nn")
    private Double slNom;

    @Column(name = "bn")
    private Double shNom;

    @Column(name = "resist")
    private Double resist;

    @Column(name = "resist_h")
    private Double resistH;

    @Column(name = "resist_m")
    private Double resistM;

    @Column(name = "resist_l")
    private Double resistL;

    @OneToMany(mappedBy = "powerTransformer")
    @MapKey(name = "lang")
    private Map<LangEnum, PowerTransformerTranslate> translates;
}
