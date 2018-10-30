package calc.formula.service;

import calc.entity.calc.asp.AspResultHeader;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.inter.InterResultHeader;
import calc.entity.calc.seg.SegResultHeader;
import java.util.Map;

public interface MessageService {
    void deleteMessages(BalanceSubstResultHeader header);
    void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode, String info);
    void addMessage(BalanceSubstResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params);

    void deleteMessages(AspResultHeader header);
    void addMessage(AspResultHeader header, Long lineNum, String docCode, String  errCode);
    void addMessage(AspResultHeader header, Long lineNum, String docCode, String  errCode, Map<String, String> params);

    void deleteMessages(SegResultHeader header);
    void addMessage(SegResultHeader header, Long lineNum, String docCode, String  errCode, Map<String, String> params);

    void deleteMessages(InterResultHeader header);
    void addMessage(InterResultHeader header, Long lineNum, String docCode, String  errCode, Map<String, String> params);
}
