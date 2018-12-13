package calc.formula.service.impl;

import calc.entity.calc.asp.*;
import calc.entity.calc.bs.*;
import calc.entity.calc.enums.*;
import calc.entity.calc.inter.*;
import calc.entity.calc.loss.*;
import calc.entity.calc.reg.*;
import calc.entity.calc.seg.*;
import calc.entity.calc.source.*;
import calc.entity.calc.svr.*;
import calc.formula.exception.*;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static calc.util.Util.buildMsgParams;

@SuppressWarnings("Duplicates")
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final BsResultMessageRepo bsResultMessageRepo;
    private final AspResultMessageRepo aspResultMessageRepo;
    private final SegResultMessageRepo segResultMessageRepo;
    private final RegResultMessageRepo regResultMessageRepo;
    private final SourceResultMessageRepo sourceResultMessageRepo;
    private final InterResultMessageRepo interResultMessageRepo;
    private final LossFactResultMessageRepo lossFactResultMessageRepo;
    private final SvrResultMessageRepo svrResultMessageRepo;
    private final MessageErrorService messageErrorService;

    @Override
    public void deleteMessages(AspResultHeader header) {
        List<AspResultMessage> lines = aspResultMessageRepo.findAllByHeaderId(header.getId());
        aspResultMessageRepo.delete(lines);
        aspResultMessageRepo.flush();
    }

    @Override
    public void deleteMessages(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMessage> lines = bsResultMessageRepo.findAllByHeaderId(header.getId());
        bsResultMessageRepo.delete(lines);
        bsResultMessageRepo.flush();
    }

    @Override
    public void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode, String info) {
        MessageError err = messageErrorService.getError(errCode);
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
        MessageError err = messageErrorService.getError(errCode);
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
    public void addMessage(AspResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = messageErrorService.getError(errCode);
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
        segResultMessageRepo.delete(lines);
        segResultMessageRepo.flush();
    }

    @Override
    public void addMessage(SegResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = messageErrorService.getError(errCode);
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
        interResultMessageRepo.delete(lines);
        interResultMessageRepo.flush();
    }

    @Override
    public void addMessage(InterResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = messageErrorService.getError(errCode);
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
            message.setCreateBy(header.getCreateBy());
            message.setCreateDate(LocalDateTime.now());

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
        lossFactResultMessageRepo.delete(lines);
        lossFactResultMessageRepo.flush();
    }

    @Override
    public void addMessage(LossFactResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = messageErrorService.getError(errCode);
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
        regResultMessageRepo.delete(lines);
        regResultMessageRepo.flush();
    }

    @Override
    public void addMessage(RegResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = messageErrorService.getError(errCode);
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
        sourceResultMessageRepo.delete(lines);
        sourceResultMessageRepo.flush();
    }

    @Override
    public void addMessage(SourceResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params) {
        MessageError err = messageErrorService.getError(errCode);
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

    @Override
    public void deleteMessages(SvrResultHeader header) {
        List<SvrResultMessage> lines = svrResultMessageRepo.findAllByHeaderId(header.getId());
        svrResultMessageRepo.delete(lines);
        svrResultMessageRepo.flush();
    }

    @Override
    public void addMessage(SvrResultHeader header, MeteringPointSetting line, CalcServiceException exc) {
        Map<String, String> params = buildMsgParams(line.getMeteringPoint());
        if (exc != null)
            params.putIfAbsent("err", exc.getMessage());

        MessageError err = messageErrorService.getError(exc.getErrCode());
        try {
            SvrResultMessage message = SvrResultMessage.of(err, params);
            message.setHeader(header);
            message.setLineNum(line.getId());
            svrResultMessageRepo.save(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
