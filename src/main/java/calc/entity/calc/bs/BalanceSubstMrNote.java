package calc.entity.calc.bs;

import calc.entity.calc.MeteringPoint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_subst_mr_notes")
@Immutable
public class BalanceSubstMrNote {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_subst_header_id")
    private BalanceSubstHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "note_order")
    private Long lineNum;

    @OneToMany(mappedBy = "note", fetch = FetchType.EAGER)
    private List<BalanceSubstMrNoteTranslate> translates;
}
