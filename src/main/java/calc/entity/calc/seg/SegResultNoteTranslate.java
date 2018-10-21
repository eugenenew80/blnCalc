package calc.entity.calc.seg;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_result_notes_tl")
public class SegResultNoteTranslate {
    @Id
    @SequenceGenerator(name="calc_balance_segment_result_notes_tl_s", sequenceName = "calc_balance_segment_result_notes_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_result_notes_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_note_id")
    private SegResultNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String noteText;
}
