package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_metering_point_meters")
@Immutable
public class MeteringPointMeter  {
	@Id
	private Long id;

	@ManyToOne
	@JoinColumn(name = "metering_point_id")
	private MeteringPoint meteringPoint;

	@ManyToOne
	@JoinColumn(name = "meter_id")
	private Meter meter;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;
}
