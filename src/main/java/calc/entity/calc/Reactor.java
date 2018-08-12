package calc.entity.calc;

import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_reactors")
@Immutable
public class Reactor {
    @Id
    private Long id;

    @Column
    private String name;

    @Column(name = "delta_pr")
    private Double deltaPr;

    @Column(name = "unom")
    private Double unom;
}
