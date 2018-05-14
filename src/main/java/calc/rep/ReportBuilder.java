package calc.rep;

import org.springframework.data.util.Pair;

import java.util.List;

public interface ReportBuilder {
    void generateTotals(Long tableId);

    void generateSectionRows(Long sectionId, List<Pair<String, String>> params);

    void generateDivisionRows(Long divisionId, List<Pair<String, String>> params);
}
