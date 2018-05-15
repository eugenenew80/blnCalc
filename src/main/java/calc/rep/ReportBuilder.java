package calc.rep;

import calc.entity.rep.*;
import org.springframework.data.util.Pair;

import java.util.List;

public interface ReportBuilder {
    Report createFromTemplate(String reportCode);

    List<TableRow> generateSectionRows(TableSection section, List<Pair<String, String>> params);

    List<TableRow> generateDivisionRows(TableDivision division, List<Pair<String, String>> params);

    TableRow generateSectionTotals(TableSection section);

    TableRow generateDivisionTotals(TableDivision division);

    List<TableCell> generateRowCells(TableRow row, Pair<String, String> param);

    List<TableCell> generateTotalCells(TableRow totalRow, List<TableRow> rows);
}
