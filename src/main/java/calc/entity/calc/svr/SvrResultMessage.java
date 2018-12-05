package calc.entity.calc.svr;

import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.MessageTypeEnum;
import calc.formula.service.MessageError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.text.StrSubstitutor;

import javax.persistence.*;
import java.util.Map;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_service_values_messages")
public class SvrResultMessage {
    @Id
    @SequenceGenerator(name="calc_service_values_messages_s", sequenceName = "calc_service_values_messages_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_service_values_messages_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_values_reconciliation_id")
    private SvrResultHeader header;

    @Column(name = "line_num")
    private Long lineNum;

    @Column(name="message_type_code")
    @Enumerated(EnumType.STRING)
    private MessageTypeEnum messageType;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "msg_text")
    private String msg;

    public static SvrResultMessage of(MessageError err, Map<String, String> params) {
        LangEnum defLang = LangEnum.RU;
        String defTExt = "Описание не найдено";
        String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
        MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;
        msg = StrSubstitutor.replace(msg, params);

        SvrResultMessage message = new SvrResultMessage();
        message.setMessageType(messageType);
        message.setErrorCode(err.getCode());
        message.setMsg(msg);

        return message;
    }
}
