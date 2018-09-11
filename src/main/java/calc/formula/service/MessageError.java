package calc.formula.service;

import calc.entity.calc.enums.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageError {
    private String code;
    private MessageTypeEnum messageType;
    private String text;
}
