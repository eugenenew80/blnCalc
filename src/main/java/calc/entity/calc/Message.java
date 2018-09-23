package calc.entity.calc;

import calc.entity.calc.enums.MessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"code"})
@Entity
@Table(name = "calc_messages")
@Immutable
public class Message {
    @Id
    private String code;

    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    private MessageTypeEnum messageType;

    @OneToMany(mappedBy = "message", fetch = FetchType.EAGER)
    private List<MessageTranslate> translates;
}
