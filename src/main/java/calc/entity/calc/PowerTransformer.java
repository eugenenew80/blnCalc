package calc.entity.calc;

import calc.entity.calc.enums.EquipmentTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
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

    @Column
    private String name;

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
    private EquipmentTypeEnum equipmentType;

    @Column(name = "nn")
    private Double slNom;

    @Column(name = "bn")
    private Double shNom;
}
