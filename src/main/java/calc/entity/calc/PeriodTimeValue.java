package calc.entity.calc;

import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.formula.CalcResult;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "media_period_time_values")
@Immutable
@NamedEntityGraph(name="PeriodTimeValue.allJoins", attributeNodes = {
	@NamedAttributeNode("sourceType"),
	@NamedAttributeNode("meteringPoint"),
	@NamedAttributeNode("param"),
	@NamedAttributeNode("unit"),
})
public class PeriodTimeValue  {
	@Id
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
	private Long interval;

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

	@Column(name="data_type")
	@Enumerated(EnumType.STRING)
	private DataTypeEnum dataType;

	public CalcResult toResult() {
		CalcResult result = new CalcResult();
		result.setParamType("PT");
		result.setPeriodType(getPeriodType());
		result.setMeteringDate(getMeteringDate());
		result.setMeteringPoint(getMeteringPoint());
		result.setParam(getParam());
		result.setUnit(getUnit());
		result.setDoubleValue(getVal());
		result.setSourceType(getSourceType());
		result.setDataType(getDataType());

		if (result.getDataType() == null)
			result.setDataType(getPeriodType() == PeriodTypeEnum.M ? DataTypeEnum.FINAL : DataTypeEnum.OPER);

		return result;
	}
}
