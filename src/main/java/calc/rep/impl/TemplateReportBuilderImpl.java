package calc.rep.impl;

import calc.entity.rep.*;
import calc.entity.rep.enums.ValueTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.rep.DocumentBuilder;
import calc.rep.TemplateReportBuilder;
import calc.repo.rep.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TemplateReportBuilderImpl implements TemplateReportBuilder {
    private final ReportRepo reportRepo;
    private final SheetRepo sheetRepo;
    private final TableRepo tableRepo;
    private final ColumnRepo columnRepo;
    private final DivisionRepo divisionRepo;
    private final SectionRepo sectionRepo;
    private final CalcService calcService;
    private final DocumentBuilder documentBuilder;

    @Override
    public Document buildReport(Long reportId, CalcContext context) throws Exception {
        Report report = reportRepo.findOne(reportId);
        Map<Long, CalcResult> results = calc(report, context);
        context.setResults(results);

        return documentBuilder.buildDocument(report, context);
    }

    @Override
    public Map<Long, CalcResult> calc(Report report, CalcContext context) {
        Map<Long, CalcResult> results = new HashMap<>();
        for (TableCell cell : report.getCells()) {
            if (cell.getAttr().getValueType()== ValueTypeEnum.FORMULA && cell.getFormula()!=null) {
                CalcResult result = null;
                try {
                    result = calcService.calc(cell.getFormula(), context);
                }
                catch (Exception e) {
                    results.put(cell.getId(), null);
                    e.printStackTrace();
                }

                results.put(cell.getId(), result);
            }
        }
        return results;
    }

    @Override
    public Map<Long, CalcResult> calc(Long reportId, CalcContext context) {
        Report report = reportRepo.findOne(reportId);
        return calc(report, context);
    }

    @Override
    public void save(Document doc, String fileName) throws Exception {
        DOMSource source = new DOMSource(doc);

        FileWriter writer = new FileWriter(new File(fileName));
        StreamResult result = new StreamResult(writer);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
    }

    @Override
    public Document transform(Document doc, Source xslSource) throws Exception {
        Transformer trans = TransformerFactory
            .newInstance()
            .newTransformer(xslSource);

        DOMSource source = new DOMSource(doc);
        DOMResult output = new DOMResult();
        trans.transform(source, output);

        return (Document) output.getNode();
    }

    @Override
    public Document transform(Document doc) throws Exception {
        return transform(doc, new StreamSource(new File("files/report.xsl")));
    }

    @Override
    public Report createFromTemplate(Long reportId) {
        Report report = reportRepo.findOne(reportId);

        Report newReport = new Report();
        newReport.setName(report.getName());
        newReport.setIsTemplate(false);
        newReport.setCode(report.getCode());
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
                    newDivision.setSheet(newSheet);
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
                        newSection.setSheet(newSheet);
                        newSection.setTable(newTable);
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

            for (SheetColumn column : sheet.getColumns()) {
                SheetColumn newColumn = new SheetColumn();
                newColumn.setReport(newReport);
                newColumn.setSheet(newSheet);
                newColumn.setOrderNum(column.getOrderNum());
                newColumn.setWidth(column.getWidth());
                columnRepo.save(newColumn);
            }
        }

        return reportRepo.findOne(newReport.getId());
    }
}
