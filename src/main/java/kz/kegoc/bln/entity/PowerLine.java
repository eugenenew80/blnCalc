package kz.kegoc.bln.entity;

import lombok.*;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_power_lines")
public class PowerLine {
    @Id
    private Long id;

    @Column
    private String code;

    @Column
    private Double length;

    @Column
    private Double r;

    @Column
    private Double po;
}
