package calc.entity.calc;

import lombok.*;
import javax.persistence.*;
import org.hibernate.annotations.Immutable;
import calc.converter.jpa.BooleanToIntConverter;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "media_source_types")
@Immutable
public class SourceType {
    @Id
    private Long id;

    @Column(name = "source_system_code")
    private String sourceSystemCode;

    @Column(name = "input_method")
    private String inputMethod;

    @Column(name = "receiving_method")
    private String receivingMethod;

    @Column(name = "is_active")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isActive;

    @Column
    private Byte priority;
}
