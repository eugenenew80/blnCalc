package calc.formula.service.impl;

import calc.entity.calc.Message;
import calc.entity.calc.MessageTranslate;
import calc.entity.calc.enums.LangEnum;
import calc.formula.service.MessageError;
import calc.formula.service.MessageErrorService;
import calc.repo.calc.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MessageErrorServiceImpl implements MessageErrorService {
    private final MessageRepo messageRepo;
    private final CacheManager ehcacheManager;

    @Override
    public MessageError getError(String errCode) {
        Cache<String, MessageError> messageCache = ehcacheManager.getCache("messageCache", String.class, MessageError.class);
        MessageError messageError = messageCache.get(errCode);

        if (messageError == null) {
            messageError = buildMessageError(errCode);
            messageCache.putIfAbsent(errCode, messageError);
        }

        return messageError;
    }

    private MessageError buildMessageError(String errCode) {
        Message message = messageRepo.findOne(errCode);

        MessageError messageError;
        Map<LangEnum, String> texts = new HashMap<>();
        for (MessageTranslate translate : message.getTranslates())
            texts.putIfAbsent(translate.getId().getLang(), translate.getText());

        messageError = new MessageError(message.getCode(), message.getMessageType(), texts);
        return messageError;
    }
}
