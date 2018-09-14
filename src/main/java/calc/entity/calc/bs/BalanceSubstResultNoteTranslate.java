package calc.entity.calc.bs;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_notes_tl")
@Immutable
public class BalanceSubstResultNoteTranslate {
    @Id
    @SequenceGenerator(name="calc_bs_result_notes_tl_s", sequenceName = "calc_bs_result_notes_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_notes_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_note_id")
    private BalanceSubstResultNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String text;
}
