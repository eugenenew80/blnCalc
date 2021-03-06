package calc.entity.calc.asp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_notes")
@Immutable
public class AspNote {
    @Id
    @SequenceGenerator(name="calc_asp1_notes_s", sequenceName = "calc_asp1_notes_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_notes_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_header_id")
    private AspHeader header;

    @Column(name = "note_num")
    private Long noteNum;

    @Column(name = "line_num")
    private Long lineNum;

    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY)
    private List<AspNoteTranslate> translates;
}
