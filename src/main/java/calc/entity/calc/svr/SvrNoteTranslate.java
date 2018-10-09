package calc.entity.calc.svr;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_svr_notes_tl")
@Immutable
public class SvrNoteTranslate {
    @Id
    @SequenceGenerator(name="calc_svr_notes_tl_s", sequenceName = "calc_svr_notes_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_svr_notes_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "svr_note_id")
    private SvrNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String noteText;
}
