package calc.entity;

import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "media_parameters")
@Immutable
public class Parameter {
    @Id
    private Long id;

    @Column
    private String code;

    @Column
    private String paramType;
}
