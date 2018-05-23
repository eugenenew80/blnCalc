package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "dict_meters")
@Immutable
public class Meter {
	@Id
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "serial_number")
	private String serialNumber;
}
