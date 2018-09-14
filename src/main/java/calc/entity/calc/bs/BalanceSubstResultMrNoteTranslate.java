package calc.entity.calc.bs;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_mr_notes_tl")
@Immutable
public class BalanceSubstResultMrNoteTranslate {
    @Id
    @SequenceGenerator(name="calc_bs_result_mr_notes_tl_s", sequenceName = "calc_bs_result_mr_notes_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_mr_notes_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_mr_note_id")
    private BalanceSubstResultMrNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String noteText;
}
