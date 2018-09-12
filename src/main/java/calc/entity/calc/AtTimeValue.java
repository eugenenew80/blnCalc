package calc.entity.calc;

import calc.entity.calc.enums.PeriodTypeEnum;
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

	@ManyToOne
	@JoinColumn(name = "metering_point_id")
	private MeteringPoint meteringPoint;

	@ManyToOne
	@JoinColumn(name = "param_id")
	private Parameter param;

	@ManyToOne
	@JoinColumn(name = "unit_id")
	private Unit unit;

	@Column(name = "metering_date")
	private LocalDateTime meteringDate;

	@Column
	private Double val;

	@Column
	private String status;

	@ManyToOne
	@JoinColumn(name = "source_type_id")
	private SourceType sourceType;

	@Column(name="period_type")
	@Enumerated(EnumType.STRING)
	private PeriodTypeEnum periodType;

	@Column(name = "source_code")
	private String sourceCode;

	public CalcResult toResult() {
		CalcResult result = new CalcResult();
		result.setParamType("AT");
		result.setPeriodType(getPeriodType());
		result.setMeteringDate(getMeteringDate());
		result.setMeteringPoint(getMeteringPoint());
		result.setParam(getParam());
		result.setUnit(getUnit());
		result.setDoubleValue(getVal());
		result.setSourceType(getSourceType());
		return result;
	}
}
