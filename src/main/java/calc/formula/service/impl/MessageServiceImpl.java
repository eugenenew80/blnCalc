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
import calc.entity.calc.inter.InterResultHeader;
import calc.entity.calc.inter.InterResultMessage;
import calc.entity.calc.inter.InterResultMessageTranslate;
import calc.entity.calc.loss.LossFactResultHeader;
import calc.entity.calc.loss.LossFactResultMessage;
import calc.entity.calc.loss.LossFactResultMessageTranslate;
import calc.entity.calc.reg.RegResultHeader;
import calc.entity.calc.reg.RegResultMessage;
import calc.entity.calc.reg.RegResultMessageTranslate;
import calc.entity.calc.seg.SegResultHeader;
import calc.entity.calc.seg.SegResultMessage;
import calc.entity.calc.seg.SegResultMessageTranslate;
import calc.entity.calc.source.SourceResultHeader;
import calc.entity.calc.source.SourceResultMessage;
import calc.entity.calc.source.SourceResultMessageTranslate;
import calc.formula.service.MessageError;
import calc.formula.service.MessageService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final BsResultMessageRepo bsResultMessageRepo;
    private final AspResultMessageRepo aspResultMessageRepo;
    private final SegResultMessageRepo segResultMessageRepo;
    private final RegResultMessageRepo regResultMessageRepo;
    private final SourceResultMessageRepo sourceResultMessageRepo;
    private final InterResultMessageRepo interResultMessageRepo;
    private final LossFactResultMessageRepo lossFactResultMessageRepo;
    private final MessageRepo messageRepo;
    private Map<String, MessageError> mapErrors = new HashMap<>();

    @PostConstruct
    public void init() {
        List<Message> messages = messageRepo.findAll();
        for (Message message : messages) {
            Map<LangEnum, String> texts = new HashMap<>();
            for (MessageTranslate translate : message.getTranslates()) {
                texts.putIfAbsent(translate.getId().getLang(), translate.getText());
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
    public void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode, String info) {
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
            message.setTranslates(new ArrayList<>());

            BalanceSubstResultMessageTranslate messageTranslate = new BalanceSubstResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(defLang);
            messageTranslate.setMsg(msg + ", " + info);
            message.getTranslates().add(messageTranslate);

            bsResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            msg = StrSubstitutor.replace(msg, params);

            BalanceSubstResultMessage message = new BalanceSubstResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);
            message.setSection(docCode);
            message.setTranslates(new ArrayList<>());

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
            message.setTranslates(new ArrayList<>());

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

    @Override
    public void addMessage(AspResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;
            msg = StrSubstitutor.replace(msg, params);

            AspResultMessage message = new AspResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);
            message.setTranslates(new ArrayList<>());

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

    @Override
    public void deleteMessages(SegResultHeader header) {
        List<SegResultMessage> lines = segResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            segResultMessageRepo.delete(lines.get(i));
        segResultMessageRepo.flush();
    }

    @Override
    public void addMessage(SegResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;
            msg = StrSubstitutor.replace(msg, params);

            SegResultMessage message = new SegResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);
            message.setTranslates(new ArrayList<>());

            SegResultMessageTranslate messageTranslate = new SegResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(defLang);
            messageTranslate.setMsg(msg);
            message.getTranslates().add(messageTranslate);
            segResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMessages(InterResultHeader header) {
        List<InterResultMessage> lines = interResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            interResultMessageRepo.delete(lines.get(i));
        interResultMessageRepo.flush();
    }

    @Override
    public void addMessage(InterResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;
            msg = StrSubstitutor.replace(msg, params);

            InterResultMessage message = new InterResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);
            message.setTranslates(new ArrayList<>());

            InterResultMessageTranslate messageTranslate = new InterResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(defLang);
            messageTranslate.setMsg(msg);
            message.getTranslates().add(messageTranslate);
            interResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMessages(LossFactResultHeader header) {
        List<LossFactResultMessage> lines = lossFactResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            lossFactResultMessageRepo.delete(lines.get(i));
        lossFactResultMessageRepo.flush();
    }

    @Override
    public void addMessage(LossFactResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;
            msg = StrSubstitutor.replace(msg, params);

            LossFactResultMessage message = new LossFactResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);
            message.setTranslates(new ArrayList<>());

            LossFactResultMessageTranslate messageTranslate = new LossFactResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(defLang);
            messageTranslate.setMsg(msg);
            message.getTranslates().add(messageTranslate);
            lossFactResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMessages(RegResultHeader header) {
        List<RegResultMessage> lines = regResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            regResultMessageRepo.delete(lines.get(i));
        regResultMessageRepo.flush();
    }

    @Override
    public void addMessage(RegResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;
            msg = StrSubstitutor.replace(msg, params);

            RegResultMessage message = new RegResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);
            message.setTranslates(new ArrayList<>());

            RegResultMessageTranslate messageTranslate = new RegResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(defLang);
            messageTranslate.setMsg(msg);
            message.getTranslates().add(messageTranslate);
            regResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMessages(SourceResultHeader header) {
        List<SourceResultMessage> lines = sourceResultMessageRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            sourceResultMessageRepo.delete(lines.get(i));
        sourceResultMessageRepo.flush();
    }

    @Override
    public void addMessage(SourceResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = mapErrors.getOrDefault(errCode, null);
        try {
            LangEnum defLang = LangEnum.RU;
            String defTExt = "Описание не найдено";
            String msg = err != null ? err.getTexts().getOrDefault(defLang, defTExt) : defTExt;
            MessageTypeEnum messageType = err != null ? err.getMessageType() : MessageTypeEnum.E;
            msg = StrSubstitutor.replace(msg, params);

            SourceResultMessage message = new SourceResultMessage();
            message.setHeader(header);
            message.setLineNum(lineNum);
            message.setMessageType(messageType);
            message.setErrorCode(errCode);
            message.setTranslates(new ArrayList<>());

            SourceResultMessageTranslate messageTranslate = new SourceResultMessageTranslate();
            messageTranslate.setMessage(message);
            messageTranslate.setLang(defLang);
            messageTranslate.setMsg(msg);
            message.getTranslates().add(messageTranslate);
            sourceResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
