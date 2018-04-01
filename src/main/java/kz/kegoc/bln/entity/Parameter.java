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
@Table(name = "media_parameters")
public class Parameter {
    @Id
    private Long id;

    @Column
    private String code;

    @Column
    private String paramType;
}
