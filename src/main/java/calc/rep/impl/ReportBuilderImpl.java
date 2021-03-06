package calc.rep.impl;

import calc.entity.rep.*;
import calc.entity.rep.enums.TablePartEnum;
import calc.entity.rep.enums.ValueTypeEnum;
import calc.rep.ReportBuilder;
import calc.repo.rep.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportBuilderImpl implements ReportBuilder {
    private final ReportRepo reportRepo;
    private final SheetRepo sheetRepo;
    private final TableRepo tableRepo;
    private final ColumnRepo columnRepo;
    private final DivisionRepo divisionRepo;
    private final SectionRepo sectionRepo;

    @Override
    public Report createFromTemplate(String reportCode, String newReportCode) {
        Report report = reportRepo.findByCodeAndIsTemplateIsTrue(reportCode);

        Report newReport = new Report();
        newReport.setName(report.getName());
        newReport.setIsTemplate(false);
        newReport.setCode(newReportCode);
        newReport = reportRepo.save(newReport);

        for (ReportSheet sheet : report.getSheets()) {
            ReportSheet newSheet = new ReportSheet();
            newSheet.setColumnCount(sheet.getColumnCount());
            newSheet.setName(sheet.getName());
            newSheet.setOrderNum(sheet.getOrderNum());
            newSheet.setRowCount(sheet.getRowCount());
            newSheet.setCode(sheet.getCode());
            newSheet.setReport(newReport);
            newSheet  = sheetRepo.save(newSheet);

            for (SheetColumn column : sheet.getColumns()) {
                SheetColumn newColumn = new SheetColumn();
                newColumn.setReport(newReport);
                newColumn.setSheet(newSheet);
                newColumn.setOrderNum(column.getOrderNum());
                newColumn.setWidth(column.getWidth());
                columnRepo.save(newColumn);
            }

            for (ReportTable table : sheet.getTables()) {
                ReportTable newTable = new ReportTable();
                newTable.setReport(newReport);
                newTable.setSheet(newSheet);
                newTable.setBodyRowTemplate(table.getBodyRowTemplate());
                newTable.setBodyTotalTemplate(table.getBodyTotalTemplate());
                newTable.setFooterRowTemplate(table.getFooterRowTemplate());
                newTable.setHeaderRowTemplate(table.getHeaderRowTemplate());
                newTable.setHasFooter(table.getHasFooter());
                newTable.setHasHeader(table.getHasHeader());
                newTable.setName(table.getName());
                newTable.setOrderNum(table.getOrderNum());
                newTable.setCode(table.getCode());
                newTable = tableRepo.save(newTable);

                for(TableDivision division : table.getDivisions() ) {
                    TableDivision newDivision = new TableDivision();
                    newDivision.setReport(newReport);
                    newDivision.setTable(newTable);
                    newDivision.setBelongTo(division.getBelongTo());
                    newDivision.setHasTitle(division.getHasTitle());
                    newDivision.setHasTotal(division.getHasTotal());
                    newDivision.setName(division.getName());
                    newDivision.setOrderNum(division.getOrderNum());
                    newDivision.setCode(division.getCode());
                    newDivision =divisionRepo.save(newDivision);

                    for (TableSection section : division.getSections()) {
                        TableSection newSection = new TableSection();
                        newSection.setReport(newReport);
                        newSection.setDivision(newDivision);
                        newSection.setHasTitle(section.getHasTitle());
                        newSection.setHasTotal(section.getHasTotal());
                        newSection.setName(section.getName());
                        newSection.setOrderNum(section.getOrderNum());
                        newSection.setCode(section.getCode());
                        sectionRepo.save(newSection);
                    }
                }
            }
        }

        return reportRepo.findOne(newReport.getId());
    }

    @Override
    public List<TableRow> createSectionRows(TableSection section, List<Pair<String, String>> params) {
        return generateSectionRows(section, params);
    }

    @Override
    public List<TableRow> createDivisionRows(TableDivision division, List<Pair<String, String>> params) {
        return generateDivisionRows(division, params);
    }

    @Override
    public List<TableRow> createSectionTotals(Report report) {
        return report.getSheets()
            .stream()
            .flatMap(t -> t.getTables().stream())
            .flatMap(t -> t.getDivisions().stream())
            .filter(t -> t.getBelongTo() == TablePartEnum.BODY && t.getHasTotal())
            .map(t -> generateDivisionTotals(t))
            .collect(Collectors.toList());
    }

    @Override
    public List<TableRow> createDivisionTotals(Report report) {
        return report.getSheets()
            .stream()
            .flatMap(t -> t.getTables().stream())
            .flatMap(t -> t.getDivisions().stream())
            .filter(t -> t.getBelongTo() == TablePartEnum.BODY)
            .flatMap(t -> t.getSections().stream())
            .filter(t -> t.getHasTotal())
            .map(t -> generateSectionTotals(t))
            .collect(Collectors.toList());
    }


    private List<TableRow> generateSectionRows(TableSection section, List<Pair<String, String>> params) {
        Long orderNum=0l;
        List<TableRow> rows = new ArrayList<>();
        for (Pair<String, String> param: params) {
            TableRow row = new TableRow();
            row.setSection(section);
            row.setDivision(section.getDivision());
            row.setReport(section.getReport());
            row.setIsTotal(false);
            row.setName(param.getFirst());
            row.setCode(param.getFirst());
            row.setOrderNum(++orderNum);
            row.setIsIncludeInTotal(true);
            row.setCells(generateRowCells(row, param));
            rows.add(row);
        }
        return rows;
    }

    private List<TableRow> generateDivisionRows(TableDivision division, List<Pair<String, String>> params) {
        Long orderNum=0l;
        List<TableRow> rows = new ArrayList<>();
        for (Pair<String, String> param: params) {
            TableRow row = new TableRow();
            row.setDivision(division);
            row.setReport(division.getReport());
            row.setIsTotal(false);
            row.setName(param.getFirst());
            row.setCode(param.getFirst());
            row.setOrderNum(++orderNum);
            row.setCells(generateRowCells(row, param));
            rows.add(row);
        }
        return rows;
    }

    private TableRow generateSectionTotals(TableSection section) {
        Long orderNum = 0l;
        TableRow row = new TableRow();
        row.setSection(section);
        row.setDivision(section.getDivision());
        row.setReport(section.getReport());
        row.setIsTotal(true);
        row.setName("Итого по подразделу " + section.getCode());
        row.setOrderNum(++orderNum);
        row.setCells(generateTotalCells(row, section.getRows()));
        return row;
    }

    private TableRow generateDivisionTotals(TableDivision division) {
        Long orderNum = 0l;
        TableRow row = new TableRow();
        row.setDivision(division);
        row.setReport(division.getReport());
        row.setIsTotal(true);
        row.setName("Всего по разделу " + division.getCode());
        row.setOrderNum(++orderNum);
        row.setCells(generateTotalCells(row, division.getRows()));
        return row;
    }

    private List<TableCell> generateRowCells(TableRow row, Pair<String, String> param) {
        List<TableCell> cells = new ArrayList<>();
        for (TableAttr attr : row.getTable().getBodyRowTemplate().getAttrs()) {
            TableCell cell = TableCell.fromAttr(row, attr);

            String val = "";
            String formula = "";

            if (attr.getValueType() == ValueTypeEnum.CONST && StringUtils.equals(attr.getName(), "orderNum") && row.getOrderNum()!=null)
                val = row.getOrderNum().toString();

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getFormulaTemplate()!=null) {
                formula = attr.getFormulaTemplate()
                    .replace("#code#",  param.getFirst())
                    .replace("#param#", param.getSecond())
                    .replace("#attr#",  attr.getName());
            }

            cell.setVal(val);
            cell.setFormula(formula);

            cells.add(cell);
        }
        return cells;
    }

    private List<TableCell> generateTotalCells(TableRow totalRow, List<TableRow> rows) {
        List<TableCell> cells = new ArrayList<>();
        for (TableAttr attr : totalRow.getTable().getBodyTotalTemplate().getAttrs()) {
            TableCell cell = TableCell.fromAttr(totalRow, attr);

            String val = "";
            String formula = "";

            if (attr.getValueType() == ValueTypeEnum.CONST && attr.getOrderNum().equals(1l))
                val = totalRow.getName();

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName()!=null )
                formula = generateTotalFormula(attr, rows);

            cell.setVal(val);
            cell.setFormula(formula);
            cells.add(cell);
        }
        return cells;
    }

    private String generateTotalFormula(TableAttr attr, List<TableRow> rows) {
        String formula = "";
        for (TableRow row : rows) {
            if (row.getIsTotal() || !row.getIsIncludeInTotal())
                continue;

            TableCell amountCell = row.getCells()
                .stream()
                .filter(c -> StringUtils.equals(c.getAttr().getName(), attr.getName()))
                .findFirst().orElse(null);

            if (amountCell!=null) {
                if (formula.equals(""))
                    formula="\n";
                formula = formula + amountCell.getFormula() + "\n";
            }
        }

        if (!formula.equals(""))
            formula = "<add>" + formula + "</add>";
        return formula;
    }
}
