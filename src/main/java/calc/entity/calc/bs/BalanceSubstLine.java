package calc.entity.calc.bs;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_subst_lines")
@Immutable
public class BalanceSubstLine {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_subst_header_id")
    private BalanceSubstHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "is_inverse")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse;

    @Column(name = "is_section_1")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection1;

    @Column(name = "is_section_2")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection2;

    @Column(name = "is_section_3")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection3;

    @Column(name = "is_section_4")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection4;

    @OneToMany(mappedBy = "line", fetch = FetchType.EAGER)
    private List<BalanceSubstLineTranslate> translates;
}
