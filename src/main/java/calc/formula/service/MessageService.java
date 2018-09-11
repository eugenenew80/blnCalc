package calc.formula.service;

import calc.entity.calc.asp.AspResultHeader;
import calc.entity.calc.bs.BalanceSubstResultHeader;

public interface MessageService {
    void deleteMessages(BalanceSubstResultHeader header, String docCode);
    void deleteMessages(AspResultHeader header, String docCode);
    void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String  errCode);
    void addMessage(AspResultHeader header, Long lineNum, String docCode, String  errCode);
}
