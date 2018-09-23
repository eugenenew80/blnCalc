package calc.formula.service.impl;

import calc.entity.calc.Message;
import calc.entity.calc.MessageTranslate;
import calc.entity.calc.asp.AspResultHeader;
import calc.entity.calc.asp.AspResultMessage;
import calc.entity.calc.asp.AspResultMessageTranslate;
import calc.entity.calc.bs.BalanceSubstResultMessageTranslate;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.BalanceSubstResultMessage;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.MessageTypeEnum;
import calc.formula.service.MessageError;
import calc.formula.service.MessageService;
import calc.repo.calc.AspResultMessageRepo;
import calc.repo.calc.BsResultMessageRepo;
import calc.repo.calc.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final BsResultMessageRepo bsResultMessageRepo;
    private final AspResultMessageRepo aspResultMessageRepo;
    private final MessageRepo messageRepo;
    private Map<String, MessageError> mapErrors = new HashMap<>();

    @PostConstruct
    public void init() {
        List<Message> messages = messageRepo.findAll();
        for (Message message : messages) {
            Map<LangEnum, String> texts = new HashMap<>();
            for (MessageTranslate translate : message.getTranslates()) {
                texts.putIfAbsent(translate.getLang(), translate.getText());
                mapErrors.put(message.getCode(), new MessageError(message.getCode(), message.getMessageType(), texts));
            }
        }
    }

    @Override
    public void deleteMessages(AspResultHeader header) {
        List<AspResultMessage> lines = aspResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            aspResultMessageRepo.delete(lines.get(i));
        aspResultMessageRepo.flush();
    }

    @Override
    public void deleteMessages(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMessage> lines = bsResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
                bsResultMessageRepo.delete(lines.get(i));
        bsResultMessageRepo.flush();
    }

    @Override
    public void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;

            BalanceSubstResultMessage message = new BalanceSubstResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);
            message.setSection(docCode);

            BalanceSubstResultMessageTranslate messageTranslate = new BalanceSubstResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(defLang);
            messageTranslate.setMsg(msg);
            message.getTranslates().add(messageTranslate);

            bsResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addMessage(AspResultHeader header, Long lineNum, String docCode, String errCode) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;

            AspResultMessage message = new AspResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);

            AspResultMessageTranslate messageTranslate = new AspResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(defLang);
            messageTranslate.setMsg(msg);
            message.getTranslates().add(messageTranslate);
            aspResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
