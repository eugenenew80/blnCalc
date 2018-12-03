package calc.entity.calc.svr;

import calc.entity.calc.Contract;
import calc.entity.calc.Organization;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_mps_notes")
@Immutable
public class MeteringPointSettingNote {
    @Id
    @SequenceGenerator(name="calc_mps_notes_s", sequenceName = "calc_mps_notes_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_mps_notes_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization org;

    @Column(name = "note_num")
    private Long noteNum;

    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY)
    private List<MeteringPointSettingNoteTranslate> translates;
}
