package calc.entity.calc.inter;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_result_notes_tl")
public class InterResultNoteTranslate {
    @Id
    @SequenceGenerator(name="calc_act_inter_pl_result_notes_tl_s", sequenceName = "calc_act_inter_pl_result_notes_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_act_inter_pl_result_notes_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_note_id")
    private InterResultNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String noteText;
}
