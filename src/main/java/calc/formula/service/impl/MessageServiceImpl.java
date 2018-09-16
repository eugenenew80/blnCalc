package calc.formula.service.impl;

import calc.entity.calc.asp.AspResultHeader;
import calc.entity.calc.asp.AspResultMessage;
import calc.entity.calc.asp.AspResultMessageTranslate;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.BalanceSubstResultMessage;
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
        mapErrors.put("PE_INPUT_NOT_FOUND",         new MessageError("PE_INPUT_NOT_FOUND",         MessageTypeEnum.W,"Не задана точка учёта на вводах"));
        mapErrors.put("PE_UNOM_NOT_FOUND",          new MessageError("PE_UNOM_NOT_FOUND",          MessageTypeEnum.W,"Не задано значение unom"));
        mapErrors.put("PE_WN_NOT_FOUND",            new MessageError("PE_WN_NOT_FOUND",            MessageTypeEnum.W,"Не задано количество обмоток в трансформаторе"));
        mapErrors.put("PE_SNOM_NOT_FOUND",          new MessageError("PE_SNOM_NOT_FOUND",          MessageTypeEnum.W,"Не задано значение snom"));
        mapErrors.put("PE_UNOMH_NOT_FOUND",         new MessageError("PE_UNOMH_NOT_FOUND",         MessageTypeEnum.W,"Не задано значение unomh"));
        mapErrors.put("PE_UAVG_NOT_FOUND",          new MessageError("PE_UAVG_NOT_FOUND",          MessageTypeEnum.W,"Не задано значение uavg"));
        mapErrors.put("MR_METER_NOT_FOUND",         new MessageError("MR_METER_NOT_FOUND",         MessageTypeEnum.W,"Не найден счётчик для точки учёта"));
        mapErrors.put("MR_METER_HISTORY_NOT_FOUND", new MessageError("MR_METER_HISTORY_NOT_FOUND", MessageTypeEnum.W,"Не задано аакт замены прибора учёта для счетчика"));
        mapErrors.put("MR_SECTION_NOT_FOUND",       new MessageError("MR_SECTION_NOT_FOUND",       MessageTypeEnum.E,"Не задан раздел для точки учёта"));
        mapErrors.put("UB_UAVG_NOT_FOUND",          new MessageError("UB_UAVG_NOT_FOUND",          MessageTypeEnum.E,"Не задан класс напряжения для точки учёта"));
        mapErrors.put("BS_MP_SECTION1_NOT_FOUND",   new MessageError("BS_MP_SECTION1_NOT_FOUND",   MessageTypeEnum.W,"Не задан точка уч1та для итогов по разделу 1 баланса"));
        mapErrors.put("BS_MP_SECTION2_NOT_FOUND",   new MessageError("BS_MP_SECTION2_NOT_FOUND",   MessageTypeEnum.W,"Не задан точка уч1та для итогов по разделу 2 баланса"));
        mapErrors.put("BS_MP_SECTION3_NOT_FOUND",   new MessageError("BS_MP_SECTION3_NOT_FOUND",   MessageTypeEnum.W,"Не задан точка уч1та для итогов по разделу 3 баланса"));
        mapErrors.put("BS_MP_SECTION4_NOT_FOUND",   new MessageError("BS_MP_SECTION4_NOT_FOUND",   MessageTypeEnum.W,"Не задан точка уч1та для итогов по разделу 4 баланса"));
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
        try {
            MessageError err = mapErrors.getOrDefault(errCode, null);
            BalanceSubstResultMessage message = new BalanceSubstResultMessage();
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
