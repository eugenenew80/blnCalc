package calc.entity.calc.inter;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_notes_tl")
@Immutable
public class InterNoteTranslate {
    @Id
    @SequenceGenerator(name="calc_act_inter_pl_notes_tl_s", sequenceName = "calc_act_inter_pl_notes_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_act_inter_pl_notes_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private InterNote note;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "note")
    private String noteText;
}
