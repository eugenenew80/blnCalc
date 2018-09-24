package calc.entity.calc;

import calc.entity.calc.enums.LangEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Data
@EqualsAndHashCode(of= {"code", "lang"})
@Embeddable
public class MessageTranslateId implements Serializable {
    @Column(name = "code")
    private String code;

    @Column(name = "lang")
    @Enumerated(EnumType.STRING)
    private LangEnum lang;
}
