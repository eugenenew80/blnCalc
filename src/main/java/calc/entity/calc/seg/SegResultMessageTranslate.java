package calc.entity.calc.seg;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_segment_result_messages_tl")
public class SegResultMessageTranslate {
    @Id
    @SequenceGenerator(name="calc_balance_segment_result_messages_tl_s", sequenceName = "calc_balance_segment_result_messages_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_segment_result_messages_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_message_id")
    private SegResultMessage message;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "msg")
    private String msg;
}
