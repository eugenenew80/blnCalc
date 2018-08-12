package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_subst_mr_lines")
public class BalanceSubstMrLine {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_subst_header_id")
    private BalanceSubstHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "is_inverse")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isInverse;

    @Column(name = "is_section1")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection1;

    @Column(name = "is_section2")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection2;

    @Column(name = "is_section3")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection3;

    @Column(name = "is_section4")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection4;

    @Column(name = "is_section5")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection5;
}
