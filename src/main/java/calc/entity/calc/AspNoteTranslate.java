package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_notes_tl")
@Immutable
public class AspNoteTranslate {
    @Id
    @SequenceGenerator(name="calc_asp1_notes_tl_s", sequenceName = "calc_asp1_notes_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_notes_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_note_id")
    private AspNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String noteText;
}
