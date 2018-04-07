package calc.entity;

import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_power_lines")
@Immutable
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
