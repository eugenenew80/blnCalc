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
    private final String code;
    private final MessageTypeEnum messageType;
    private final Map<LangEnum, String> texts;
}
