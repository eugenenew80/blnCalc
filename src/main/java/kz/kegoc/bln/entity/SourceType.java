package kz.kegoc.bln.entity;

import kz.kegoc.bln.ejb.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "media_source_types")
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
}
