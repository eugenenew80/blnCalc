package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.MessageTypeEnum;
import calc.formula.service.MessageError;
import calc.formula.service.MessageService;
import calc.repo.calc.AspResultMessageRepo;
import calc.repo.calc.BsResultMessageRepo;
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
    private Map<String, MessageError> mapErrors = new HashMap<>();

    @PostConstruct
    public void init() {
        mapErrors.put("PE_INPUT_NOT_FOUND",      new MessageError("PE_INPUT_NOT_FOUND",      MessageTypeEnum.W,"Не задана точка учёта на вводах"));
        mapErrors.put("UNOM_NOT_FOUND",          new MessageError("UNOM_NOT_FOUND",          MessageTypeEnum.W,"Не задано значение unom"));
        mapErrors.put("WN_NOT_FOUND",            new MessageError("WN_NOT_FOUND",            MessageTypeEnum.W,"Не количество обмоток в трансформаторе"));
        mapErrors.put("SNOM_NOT_FOUND",          new MessageError("SNOM_NOT_FOUND",          MessageTypeEnum.W,"Не задано значение snom"));
        mapErrors.put("UNOMH_NOT_FOUND",         new MessageError("UNOMH_NOT_FOUND",         MessageTypeEnum.W,"Не задано значение unomh"));
        mapErrors.put("UAVG_NOT_FOUND",          new MessageError("UAVG_NOT_FOUND",          MessageTypeEnum.W,"Не задано значение uavg"));
        mapErrors.put("METER_NOT_FOUND",         new MessageError("METER_NOT_FOUND",         MessageTypeEnum.W,"Не найден счётчик для точки учёта"));
        mapErrors.put("METER_HISTORY_NOT_FOUND", new MessageError("METER_HISTORY_NOT_FOUND", MessageTypeEnum.W,"Не задано аакт замены прибора учёта для счетчика"));
    }

    @Override
    public void deleteMessages(AspResultHeader header, String docCode) {
        List<AspResultMessage> lines = aspResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            aspResultMessageRepo.delete(lines.get(i));
        aspResultMessageRepo.flush();
    }

    @Override
    public void deleteMessages(BalanceSubstResultHeader header, String docCode) {
        List<BsResultMessage> lines = bsResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            bsResultMessageRepo.delete(lines.get(i));
        bsResultMessageRepo.flush();
    }

    @Override
    public void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode) {
        try {
            MessageError err = mapErrors.getOrDefault(errCode, null);
            BsResultMessage message = new BsResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(err != null ? err.getMessageType() : MessageTypeEnum.E);
            message.setErrorCode(errCode);
            message.setMsgText(err != null ? err.getText() : "Описание не найдено");
            message.setSection(docCode);
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
            AspResultMessage message = new AspResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(err != null ? err.getMessageType() : MessageTypeEnum.E);
            message.setErrorCode(errCode);

            AspResultMessageTranslate messageTranslate = new AspResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(LangEnum.RU);
            messageTranslate.setMsg(err != null ? err.getText() : "Описание не найдено");
            message.getTranslates().add(messageTranslate);

            aspResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
