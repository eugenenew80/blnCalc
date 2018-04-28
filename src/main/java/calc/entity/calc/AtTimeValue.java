package calc.entity.calc;

import calc.formula.CalcResult;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "media_at_time_values")
@NamedEntityGraph(name="AtTimeValue.allJoins", attributeNodes = {
	@NamedAttributeNode("sourceType")
})
public class AtTimeValue {
	@Id
	@SequenceGenerator(name="media_at_time_values_s", sequenceName = "media_at_time_values_s", allocationSize=1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "media_at_time_values_s")
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
	private Double val;

	@Column
	private String status;

	@ManyToOne
	@JoinColumn(name = "source_type_id")
	private SourceType sourceType;

	public CalcResult toResult() {
		CalcResult result = new CalcResult();
		result.setInterval(null);
		result.setMeteringDate(this.getMeteringDate());
		result.setMeteringPointId(this.getMeteringPointId());
		result.setParamId(this.getParamId());
		result.setParamType("AT");
		result.setUnitId(this.getUnitId());
		result.setVal(this.getVal());
		result.setSourceType(this.getSourceType());
		return result;
	}
}
