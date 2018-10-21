package calc.formula.service;

import calc.entity.calc.seg.SegResultLine;
import java.util.List;

public interface SegResultService {
    List<SegResultLine> getValues(Long headerId, String mpCode);
    List<SegResultLine> getValues(Long headerId, String mpCode, String paramCode);
}
