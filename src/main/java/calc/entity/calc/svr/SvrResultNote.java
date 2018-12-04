package calc.entity.calc.svr;

import calc.entity.calc.Organization;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_svr_notes")
@Immutable
public class SvrResultNote {
    @Id
    @SequenceGenerator(name="calc_svr_notes_s", sequenceName = "calc_svr_notes_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_svr_notes_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "svr_id")
    private SvrResultHeader header;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization org;

    @Column(name = "note_num")
    private Long noteNum;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SvrResultNoteTranslate> translates;
}
