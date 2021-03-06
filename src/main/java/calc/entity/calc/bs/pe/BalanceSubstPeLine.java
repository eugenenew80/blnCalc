package calc.entity.calc.bs.pe;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.TemplateLine;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.PowerTransformer;
import calc.entity.calc.Reactor;
import calc.entity.calc.bs.BalanceSubstHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_subst_pe_lines")
public class BalanceSubstPeLine implements TemplateLine {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_subst_header_id")
    private BalanceSubstHeader header;

    @ManyToOne
    @JoinColumn(name = "reactor_id")
    private Reactor reactor;

    @ManyToOne
    @JoinColumn(name = "power_transformer_id")
    private PowerTransformer powerTransformer;

    @Column(name = "is_balance")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBalance;

    @Column(name = "is_losses")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isLosses;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_out")
    private MeteringPoint meteringPointOut;

    @Column(name = "calc_num")
    private Long calcNum;

    @Transient
    public Long getLineNum() {
        return id;
    }
}
