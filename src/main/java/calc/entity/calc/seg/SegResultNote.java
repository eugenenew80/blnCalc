package calc.entity.calc.seg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_result_notes")
public class SegResultNote {
    @Id
    @SequenceGenerator(name="calc_balance_segment_result_notes_s", sequenceName = "calc_balance_segment_result_notes_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_result_notes_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private SegResultHeader header;

    @Column(name = "note_num")
    private Long noteNum;

    @Column(name = "line_num")
    private Long lineNum;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SegResultNoteTranslate> translates;
}
