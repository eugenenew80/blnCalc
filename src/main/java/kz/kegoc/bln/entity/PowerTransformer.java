package kz.kegoc.bln.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_power_transformers")
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
