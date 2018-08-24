package calc.entity.calc;

import calc.converter.jpa.BooleanToIntConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "mdfem_eem_base")
@Immutable
public class Meter {
	@Id
	private Long id;

	@Column(name = "serial_number")
	private String serialNumber;

	@Column(name = "is_parameter_ap")
	@Convert(converter = BooleanToIntConverter.class)
	private Boolean isAp;

	@Column(name = "is_parameter_am")
	@Convert(converter = BooleanToIntConverter.class)
	private Boolean isAm;

	@Column(name = "is_parameter_rp")
	@Convert(converter = BooleanToIntConverter.class)
	private Boolean isRp;

	@Column(name = "is_parameter_rm")
	@Convert(converter = BooleanToIntConverter.class)
	private Boolean isRm;

	@ManyToOne
	@JoinColumn(name = "eem_type_id")
	private EemType eemType;
}
