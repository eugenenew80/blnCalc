package calc.formula.service;

import calc.entity.calc.BalanceSubstResultHeader;

public interface MessageService {
    void deleteMessages(BalanceSubstResultHeader header);
    void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String  errCode);
}
