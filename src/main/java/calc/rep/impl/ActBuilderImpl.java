package calc.rep.impl;

import calc.entity.rep.*;
import calc.entity.rep.enums.ValueTypeEnum;
import calc.rep.ReportBuilder;
import calc.repo.rep.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActBuilderImpl implements ReportBuilder {
    private final TableRepo tableRepo;
    private final SectionRepo sectionRepo;
    private final RowRepo rowRepo;
    private final CellRepo cellRepo;

    @Override
    public void createRows(Long sectionId, List<String> keys, String paramCode) {
        TableSection section = sectionRepo.findOne(sectionId);

        Long orderNum=0l;
        for (String key: keys) {
            TableRow row = new TableRow();
            row.setSection(section);
            row.setDivision(section.getDivision());
            row.setTable(section.getTable());
            row.setSheet(section.getSheet());
            row.setReport(section.getReport());
            row.setIsTotal(false);
            row.setName(key);
            row.setKey(key);
            row.setOrderNum(++orderNum);
            rowRepo.save(row);
            createCells(row, paramCode);
        }

        if (section.getHasTotal())
            createTotals(section);
    }

    private void createTotals(TableSection section) {
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
            createTotalCells(row);
        }
    }


    private void createTotalCells(TableRow totalRow) {
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
                        continue;;

                    TableCell amountCell = row.getCells()
                        .stream()
                        .filter(c -> c.getAttr().getName() != null && c.getAttr().getName().equals("amount"))
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

    private void createCells(TableRow row, String paramCode) {
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
                formula = "<mp code=\"" + row.getKey() + "\" attr=\"name\" />";

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName().equals("end"))
                formula = "<at mp=\"" + row.getKey() + "\" param=\"" + paramCode + "\" per=\"end\" />";

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName().equals("start"))
                formula = "<at mp=\"" + row.getKey() + "\" param=\"" + paramCode + "\" per=\"start\" />";

            if (attr.getValueType() == ValueTypeEnum.FORMULA && attr.getName().equals("amount"))
                formula = "<subtract><at mp=\"" + row.getKey() + "\" param=\"" + paramCode + "\" per=\"end\" /> <at mp=\"" + row.getKey() + "\" param=\"" + paramCode + "\" per=\"start\" /></subtract>";

            cell.setFormula(formula);
            cellRepo.save(cell);
        }
    }

}
