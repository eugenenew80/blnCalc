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
    private final ReportRepo reportRepo;
    private final SectionRepo sectionRepo;
    private final RowRepo rowRepo;
    private final CellRepo cellRepo;

    @Override
    public void createRows(Long sectionId, List<String> keys) {
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
        }
    }

    public void createTotals(Long tableId ) {
        Report report = reportRepo.findOne(reportId);

        for (report.)

    }


    @Override
    public void createCells(Long sectionId, String paramCode) {
        TableSection section = sectionRepo.findOne(sectionId);

        for (TableRow row: section.getRows()) {
            for (TableAttr attr : section.getTable().getBodyRowTemplate().getAttrs()) {
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


}
