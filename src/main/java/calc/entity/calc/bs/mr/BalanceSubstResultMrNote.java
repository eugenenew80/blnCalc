package calc.entity.calc.bs.mr;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_mr_notes")
public class BalanceSubstResultMrNote {
    @Id
    @SequenceGenerator(name="calc_bs_result_mr_notes_s", sequenceName = "calc_bs_result_mr_notes_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_mr_notes_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bs_result_header_id")
    private BalanceSubstResultHeader header;

    @ManyToOne
    @JoinColumn(name = "metering_point_id")
    private MeteringPoint meteringPoint;

    @Column(name = "note_order")
    private Long lineNum;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BalanceSubstResultMrNoteTranslate> translates;
}
