package calc.entity.calc.bs.ub;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.bs.BalanceSubstHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_subst_ub_lines")
public class BalanceSubstUbLine {
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

    @Column(name = "is_section_1")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection1;

    @Column(name = "is_section_2")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isSection2;

    @ManyToOne
    @JoinColumn(name = "param_id")
    private Parameter param;

    @Column(name = "rate")
    private Double rate;
}
