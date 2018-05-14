package calc.rep;

import java.util.List;

public interface ReportBuilder {
    void createRows(Long sectionId, List<String> keys, String paramCode);
}
