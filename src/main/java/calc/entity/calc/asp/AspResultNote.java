package calc.entity.calc.asp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_result_notes")
public class AspResultNote {
    @Id
    @SequenceGenerator(name="calc_asp1_result_notes_s", sequenceName = "calc_asp1_result_notes_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_result_notes_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_result_header_id")
    private AspResultHeader header;

    @Column(name = "note_num")
    private Long noteNum;

    @Column(name = "line_num")
    private Long lineNum;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AspResultNoteTranslate> translates;
}
