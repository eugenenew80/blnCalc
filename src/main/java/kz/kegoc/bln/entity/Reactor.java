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
public class Reactor {
    @Id
    private Long id;

    @Column(name = "delta_pr")
    private Double deltaPr;

    @Column(name = "unom")
    private Double unom;
}
