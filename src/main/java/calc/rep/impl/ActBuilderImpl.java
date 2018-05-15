package calc.rep.impl;

import calc.entity.rep.*;
import calc.entity.rep.enums.ValueTypeEnum;
import calc.rep.ReportBuilder;
import calc.repo.rep.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActBuilderImpl implements ReportBuilder {
    private final TableRepo tableRepo;
    private final DivisionRepo divisionRepo;
    private final SectionRepo sectionRepo;
    private final RowRepo rowRepo;
    private final CellRepo cellRepo;

    @Override
    public void generateTotals(Long tableId) {
        ReportTable table = tableRepo.findOne(tableId);
        for (TableDivision division : table.getDivisions()) {
            for (TableSection section : division.getSections()) {
                if (section.getHasTotal())
                    generateSectionTotals(section);
            }
            if (division.getHasTotal())
                generateDivisionTotals(division);
        }
    }

    @Override
    public void generateSectionRows(Long sectionId, List<Pair<String, String>> params) {
        TableSection section = sectionRepo.findOne(sectionId);
        Long orderNum=0l;
        for (Pair<String, String> param: params) {
            TableRow row = new TableRow();
            row.setSection(section);
            row.setDivision(section.getDivision());
            row.setTable(section.getTable());
            row.setSheet(section.getSheet());
            row.setReport(section.getReport());
            row.setIsTotal(false);
            row.setName(param.getFirst());
            row.setCode(param.getFirst());
            row.setOrderNum(++orderNum);
            rowRepo.save(row);
            generateRowCells(row, param.getSecond());
        }
    }

    @Override
    public void generateDivisionRows(Long divisionId, List<Pair<String, String>> params) {
        TableDivision division = divisionRepo.findOne(divisionId);
        Long orderNum=0l;
        for (Pair<String, String> param: params) {
            TableRow row = new TableRow();
            row.setDivision(division);
            row.setTable(division.getTable());
            row.setSheet(division.getSheet());
            row.setReport(division.getReport());
            row.setIsTotal(false);
            row.setName(param.getFirst());
            row.setCode(param.getFirst());
            row.setOrderNum(++orderNum);
            rowRepo.save(row);
            generateRowCells(row, param.getSecond());
        }
    }

    private void generateSectionTotals(TableSection section) {
        Long orderNum = 0l;
        if (section.getHasTotal()) {
            TableRow row = new TableRow();
            row.setSection(section);
            row.setDivision(section.getDivision());
            row.setTable(section.getTable());
            row.setSheet(section.getSheet());
            row.setReport(section.getReport());
            row.setIsTotal(true);
            row.setName("Итого по подразделу " + section.getName());
            row.setOrderNum(++orderNum);
            rowRepo.save(row);
            generateTotalCells(row);
        }
    }

    private void generateDivisionTotals(TableDivision division) {
        Long orderNum = 0l;
        if (division.getHasTotal()) {
            TableRow row = new TableRow();
            row.setDivision(division);
            row.setTable(division.getTable());
            row.setSheet(division.getSheet());
            row.setReport(division.getReport());
            row.setIsTotal(true);
            row.setName("Всего по разделу " + division.getName());
            row.setOrderNum(++orderNum);
            rowRepo.save(row);
            generateTotalCells(row);
        }
    }

    private void generateTotalCells(TableRow totalRow) {
        for (TableAttr attr : totalRow.getTable().getBodyTotalTemplate().getAttrs()) {
            TableCell cell = new TableCell();
            cell.setReport(totalRow.getReport());
            cell.setSheet(totalRow.getSheet());
            cell.setTable(totalRow.getTable());
            cell.setDivision(totalRow.getDivision());
            cell.setSection(totalRow.getSection());
            cell.setRow(totalRow);
            cell.setAttr(attr);

            String val = "";
            String formula = "";

            if (attr.getValueType() == ValueTypeEnum.CONST && attr.getName().equals("name"))
                val = totalRow.getName();

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName().equals("amount")) {
                formula =  "<add>";
                for (TableRow row : totalRow.getSection().getRows()) {
                    if (row.getIsTotal())
                        continue;

                    if (!row.getIsIncludeInTotal())
                        continue;

                    TableCell amountCell = row.getCells()
                        .stream()
                        .filter(c -> c.getAttr().getName() != null && c.getAttr().getName().equals(attr.getName()))
                        .findFirst().orElse(null);

                    if (amountCell!=null)
                        formula = formula + amountCell.getFormula();
                }
                formula = formula + "</add>";
            }

            cell.setVal(val);
            cell.setFormula(formula);

            cellRepo.save(cell);
        }
    }

    private void generateRowCells(TableRow row, String paramCode) {
        for (TableAttr attr : row.getTable().getBodyRowTemplate().getAttrs()) {
            TableCell cell = new TableCell();
            cell.setReport(row.getReport());
            cell.setSheet(row.getSheet());
            cell.setTable(row.getTable());
            cell.setDivision(row.getDivision());
            cell.setSection(row.getSection());
            cell.setRow(row);
            cell.setAttr(attr);

            String formula = "";
            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName().equals("name"))
                formula = "<mp code=\"" + row.getCode() + "\" attr=\"name\" />";

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName().equals("end"))
                formula = "<at mp=\"" + row.getCode() + "\" param=\"" + paramCode + "\" per=\"end\" />";

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName().equals("start"))
                formula = "<at mp=\"" + row.getCode() + "\" param=\"" + paramCode + "\" per=\"start\" />";

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName().equals("amount"))
                formula = "<subtract><at mp=\"" + row.getCode() + "\" param=\"" + paramCode + "\" per=\"end\" /> <at mp=\"" + row.getCode() + "\" param=\"" + paramCode + "\" per=\"start\" /></subtract>";

            cell.setFormula(formula);
            cellRepo.save(cell);
        }
    }
}
