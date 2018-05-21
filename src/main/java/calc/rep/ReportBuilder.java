package calc.rep;

import calc.entity.rep.*;
import org.springframework.data.util.Pair;

import java.util.List;

public interface ReportBuilder {
    Report createFromTemplate(String reportCode, String newReportCode);

    List<TableRow> createSectionRows(TableSection section, List<Pair<String, String>> params);

    List<TableRow> createDivisionRows(TableDivision division, List<Pair<String, String>> params);

    List<TableRow> createSectionTotals(Report report);

    List<TableRow> createDivisionTotals(Report report);
}
