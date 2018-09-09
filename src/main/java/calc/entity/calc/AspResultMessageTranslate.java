package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_result_messages_tl")
public class AspResultMessageTranslate {
    @Id
    @SequenceGenerator(name="calc_asp1_result_messages_tl_s", sequenceName = "calc_asp1_result_messages_tl_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_result_messages_tl_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_result_message_id")
    private AspResultMessage message;

    @Column(name="lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "msg")
    private String msg;
}
