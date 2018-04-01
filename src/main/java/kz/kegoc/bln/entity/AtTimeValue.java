package kz.kegoc.bln.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "media_at_time_values")
public class AtTimeValue {
	@Id
	private Long id;

	@Column(name = "metering_point_id")
	private Long meteringPointId;

	@Column(name = "param_id")
	private Long paramId;

	@Column(name = "metering_date")
	private LocalDateTime meteringDate;

	@Column
	private Double val;

	@Column
	private String status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_type_id")
	private SourceType sourceType;
}
