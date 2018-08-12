package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_task_params")
public class TaskParam {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name="task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name="org_id")
    private Organization org;

    @ManyToOne
    @JoinColumn(name="substation_id")
    private Substation substation;

    @ManyToOne
    @JoinColumn(name="reactor_id")
    private Reactor reactor;

    @ManyToOne
    @JoinColumn(name="power_transformer_id")
    private PowerTransformer powerTransformer;

    @ManyToOne
    @JoinColumn(name="power_line_id")
    private PowerLine powerLine;

    @ManyToOne
    @JoinColumn(name="metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
