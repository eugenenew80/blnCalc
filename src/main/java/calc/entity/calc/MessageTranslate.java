package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_messages_tl")
@Immutable
public class MessageTranslate {
    @EmbeddedId
    private MessageTranslateId id;

    @ManyToOne
    @JoinColumn(name = "code", insertable = false, updatable = false)
    private Message message;

    @Column(name="lang", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private LangEnum lang;

    @Column(name = "text")
    private String text;
}
