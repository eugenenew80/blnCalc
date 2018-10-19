package calc.entity.calc.seg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_notes")
@Immutable
public class SegNote {
    @Id
    @SequenceGenerator(name="calc_balance_segment_notes_s", sequenceName = "calc_balance_segment_notes_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_notes_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private SegHeader header;

    @Column(name = "note_num")
    private Long noteNum;

    @Column(name = "line_num")
    private Long lineNum;

    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY)
    private List<SegNoteTranslate> translates;
}
