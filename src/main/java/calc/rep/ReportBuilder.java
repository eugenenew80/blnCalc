package calc.rep;

import calc.entity.rep.*;
import org.springframework.data.util.Pair;

import java.util.List;

public interface ReportBuilder {
    Report createFromTemplate(String reportCode, String newReportCode);

    void createSectionRows(TableSection section, List<Pair<String, String>> params);

    void createDivisionRows(TableSection section, List<Pair<String, String>> params);

    void createTotals(Report report);
}
