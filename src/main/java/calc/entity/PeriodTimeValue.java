package calc.entity;

import calc.controller.rest.dto.Result;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "media_period_time_values")
@NamedEntityGraph(name="PeriodTimeValue.allJoins", attributeNodes = {
	@NamedAttributeNode("sourceType")
})
public class PeriodTimeValue  {
	@Id
	@SequenceGenerator(name="media_period_time_values_s", sequenceName = "media_period_time_values_s", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "media_period_time_values_s")
	private Long id;

	@Column(name = "metering_point_id")
	private Long meteringPointId;

	@Column(name = "param_id")
	private Long paramId;

	@Column(name = "unit_id")
	private Long unitId;

	@Column(name = "metering_date")
	private LocalDateTime meteringDate;

	@Column
	private Long interval;

	@Column
	private Double val;

	@Column
	private String status;

	@ManyToOne
	@JoinColumn(name = "source_type_id")
	private SourceType sourceType;

	public Result toResult() {
		Result result = new Result();
		result.setInterval(this.getInterval());
		result.setMeteringDate(this.getMeteringDate());
		result.setMeteringPointId(this.getMeteringPointId());
		result.setParamId(this.getParamId());
		result.setParamType("PT");
		result.setUnitId(this.getUnitId());
		result.setVal(this.getVal());
		result.setSourceType(this.getSourceType());
		return result;
	}
}
