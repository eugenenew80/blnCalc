package calc.entity.calc.inter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_notes")
@Immutable
public class InterNote {
    @Id
    @SequenceGenerator(name="calc_act_inter_pl_notes_s", sequenceName = "calc_act_inter_pl_notes_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_act_inter_pl_notes_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private InterHeader header;

    @Column(name = "note_num")
    private Long noteNum;

    @Column(name = "line_num")
    private Long lineNum;

    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY)
    private List<InterNoteTranslate> translates;
}
