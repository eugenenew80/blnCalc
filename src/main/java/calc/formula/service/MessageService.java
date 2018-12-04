package calc.formula.service;

import calc.entity.calc.asp.AspResultHeader;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.source.SourceResultHeader;
import calc.entity.calc.inter.InterResultHeader;
import calc.entity.calc.loss.LossFactResultHeader;
import calc.entity.calc.reg.RegResultHeader;
import calc.entity.calc.seg.SegResultHeader;
import calc.entity.calc.svr.MeteringPointSetting;
import calc.entity.calc.svr.SvrResultHeader;
import calc.formula.exception.CalcServiceException;

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

    void deleteMessages(LossFactResultHeader header);
    void addMessage(LossFactResultHeader header, Long lineNum, String docCode, String errCode, Map<String, String> params);

    void deleteMessages(RegResultHeader header);
    void addMessage(RegResultHeader header, Long lineNum, String docCode, String  errCode, Map<String, String> params);

    void deleteMessages(SourceResultHeader header);
    void addMessage(SourceResultHeader header, Long lineNum, String docCode, String  errCode, Map<String, String> params);

    void deleteMessages(SvrResultHeader header);
    void addMessage(SvrResultHeader header, MeteringPointSetting line, CalcServiceException exc);
}
