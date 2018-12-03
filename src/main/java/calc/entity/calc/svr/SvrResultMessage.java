package calc.entity.calc.svr;

import calc.entity.calc.enums.MessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

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
    @JoinColumn(name = "result_header_id")
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
}
