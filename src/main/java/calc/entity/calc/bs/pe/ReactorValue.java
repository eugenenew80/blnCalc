package calc.entity.calc.bs.pe;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Reactor;
import calc.entity.calc.Unit;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_reactor_values")
public class ReactorValue {
    @Id
    @SequenceGenerator(name="calc_reactor_values_s", sequenceName = "calc_reactor_values_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_reactor_values_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_header_id")
    private BalanceSubstResultHeader header;

    @Column(name = "val")
    private Double val;

    @Column(name = "delta_pr")
    private Double deltaPr;

    @Column(name = "unom")
    private Double unom;

    @Column(name = "uavg")
    private Double uavg;

    @Column(name = "operating_time")
    private Double operatingTime;

    @ManyToOne
    @JoinColumn(name = "reactor_id")
    private Reactor reactor;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "input_mp_id")
    private MeteringPoint inputMp;

    @Column(name = "is_balance")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isBalance;

    @ManyToOne
    @JoinColumn(name = "metering_point_id_out")
    private MeteringPoint meteringPointOut;

    @Column(name = "is_losses")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isLosses;
}

