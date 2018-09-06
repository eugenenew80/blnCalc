package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "cm_doc_keg_cte")
@Immutable
public class ContractKeg {

    @Id
    private Long id;

    @Column(name = "doc_number")
    private String contractNum;

    @Column(name = "doc_date")
    private LocalDate docDate;
}
