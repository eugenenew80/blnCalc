package calc.formula.service;

import calc.entity.calc.asp.AspResultHeader;
import calc.entity.calc.bs.BalanceSubstResultHeader;

import java.util.Map;

public interface MessageService {
    void deleteMessages(BalanceSubstResultHeader header);
    void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode, String info);
    void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params);

    void deleteMessages(AspResultHeader header);
    void addMessage(AspResultHeader header, Long lineNum, String docCode, String  errCode);
}
