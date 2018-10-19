package calc.entity.calc.seg;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_notes_tl")
@Immutable
public class SegNoteTranslate {
    @Id
    @SequenceGenerator(name="calc_balance_segment_notes_tl_s", sequenceName = "calc_balance_segment_notes_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_notes_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private SegNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String noteText;
}
