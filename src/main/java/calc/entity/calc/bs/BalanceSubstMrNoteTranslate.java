package calc.entity.calc.bs;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_subst_mr_notes_tl")
@Immutable
public class BalanceSubstMrNoteTranslate {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "balance_subst_mr_note_id")
    private BalanceSubstMrNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String noteText;
}
