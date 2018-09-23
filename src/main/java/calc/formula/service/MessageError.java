package calc.formula.service;

import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class MessageError {
    private String code;
    private MessageTypeEnum messageType;
    private Map<LangEnum, String> texts;
}
