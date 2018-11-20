package calc.entity.calc.source;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_balance_source_result_messages_tl")
public class SourceResultMessageTranslate {
    @Id
    @SequenceGenerator(name="calc_balance_source_result_messages_tl_s", sequenceName = "calc_balance_source_result_messages_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_balance_source_result_messages_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_message_id")
    private SourceResultMessage message;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "msg")
    private String msg;
}
