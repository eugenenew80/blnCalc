package calc.rep.impl;

import calc.entity.rep.*;
import calc.entity.rep.enums.AttrTypeEnum;
import calc.entity.rep.enums.TablePartEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.rep.ReportBuilder;
import calc.repo.rep.ReportRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ReportBuilderImpl implements ReportBuilder {
    private final ReportRepo reportRepo;
    private final CalcService calcService;

    @Override
    public Document buildReport(Long reportId, CalcContext context) throws Exception {
        Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();

        Report report = reportRepo.findOne(reportId);
        Document result = createReportElement(report, doc, context);

        return result;
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
        return transform(doc,  new StreamSource(new File("files/report.xsl")));
    }


    private Document createReportElement(Report report, Document doc, CalcContext context)  {
        Element reportElement = doc.createElement("report");
        reportElement.setAttribute("type", report.getReportType());

        List<Element> sheetElements = createSheetElements(doc, report.getSheets(), context);
        for (Element sheetElement : sheetElements)
            reportElement.appendChild(sheetElement);

        doc.appendChild(reportElement);
        return doc;
    }

    private List<Element> createSheetElements(Document doc, List<ReportSheet> sheets, CalcContext context) {
        return sheets.stream()
            .sorted(Comparator.comparing(ReportSheet::getOrderNum))
            .map(sheet -> createSheetElement(doc, sheet, context))
            .collect(toList());
    }

    private Element createSheetElement(Document doc, ReportSheet sheet, CalcContext context) {
        Element sheetElement = doc.createElement("sheet");
        sheetElement.setAttribute("name", sheet.getName());

        sheet.getColumns()
            .stream()
            .sorted(Comparator.comparing(SheetColumn::getOrderNum))
            .forEach(column -> {
                Element columnElement = doc.createElement("column");
                columnElement.setAttribute("width", column.getWidth().toString());
                sheetElement.appendChild(columnElement);
            });

        Element sheetHeaderElement = createSheetHeaderElement(doc, context);
        sheetElement.appendChild(sheetHeaderElement);

        List<Element> tableElements = createTableElements(doc, sheet.getTables(), context);
        for (Element tableElement : tableElements)
            sheetElement.appendChild(tableElement);

        return sheetElement;
    }

    private Element createSheetHeaderElement(Document doc, CalcContext context) {
        Element headElement = doc.createElement("head");
        headElement.setAttribute("type", context.getReportType());
        headElement.setAttribute("name", context.getReportName());

        Element repPeriodElement = doc.createElement("period");
        repPeriodElement.setAttribute("start-date", context.getStartDate().format(DateTimeFormatter.ISO_DATE));
        repPeriodElement.setAttribute("end-date", context.getEndDate().format(DateTimeFormatter.ISO_DATE));
        headElement.appendChild(repPeriodElement);

        Element repEnergyObjectElement = doc.createElement("energy-object");
        repEnergyObjectElement.setAttribute("type", context.getEnergyObjectType());
        repEnergyObjectElement.setAttribute("name", context.getEnergyObjectName());
        headElement.appendChild(repEnergyObjectElement);

        return headElement;
    }

    private List<Element> createTableElements(Document doc, List<ReportTable> tables, CalcContext context) {
        return tables.stream()
            .sorted(Comparator.comparing(ReportTable::getOrderNum))
            .map(table -> createTableElement(doc, table, context))
            .collect(toList());
    }

    private Element createTableElement(Document doc, ReportTable table, CalcContext context) {
        Element tableElement = doc.createElement("table");
        tableElement.setAttribute("name", table.getName());

        if (table.getHasHeader()) {
            Element tableHeadElement = createTableHeaderElement(doc, table);
            tableElement.appendChild(tableHeadElement);

            Element bodyElement = doc.createElement("body");
            tableElement.appendChild(bodyElement);

            List<TableDivision> divisions = table.getDivisions()
                .stream()
                .filter(t -> t.getBelongTo() == TablePartEnum.BODY)
                .collect(toList());

            List<Element> divisionElements = createDivisionElements(doc, divisions, context);
            for (Element divisionElement : divisionElements)
                bodyElement.appendChild(divisionElement);

            Element footerElement = doc.createElement("footer");
            tableElement.appendChild(footerElement);

            divisions = table.getDivisions()
                .stream()
                .filter(t -> t.getBelongTo() == TablePartEnum.FOOTER)
                .collect(toList());

            divisionElements = createDivisionElements(doc, divisions, context);
            for (Element divisionElement : divisionElements)
                footerElement.appendChild(divisionElement);
        }

        return tableElement;
    }

    private Element createTableHeaderElement(Document doc, ReportTable table) {
        Element tableHeadElement  = doc.createElement("head");
        table.getBodyRowTemplate()
            .getAttrs()
            .stream()
            .sorted(Comparator.comparing(TableAttr::getOrderNum))
            .forEach(attr -> {
                Element tableColumnElement = doc.createElement("column");
                tableColumnElement.setAttribute("type", attr.getAttrType().toString().toLowerCase());
                tableColumnElement.setAttribute("attr", attr.getName());
                tableColumnElement.setAttribute("name", attr.getDescription());
                tableHeadElement.appendChild(tableColumnElement);
            });

        return tableHeadElement;
    }


    private List<Element> createDivisionElements(Document doc, List<TableDivision> divisions, CalcContext context) {
        return divisions.stream()
            .sorted(Comparator.comparing(TableDivision::getOrderNum))
            .map(division -> createDivisionElement(doc, division, context))
            .collect(toList());
    }

    private Element createDivisionElement(Document doc, TableDivision division, CalcContext context) {
        Element divisionElement = doc.createElement("division");
        divisionElement.setAttribute("name", division.getName());
        divisionElement.setAttribute("is-total", division.getHasTotal().toString().toLowerCase());
        divisionElement.setAttribute("is-title", division.getHasTitle().toString().toLowerCase());

        List<Element> sectionElements = createSectionElements(doc, division.getSections(), context);
        for (Element sectionElement : sectionElements)
            divisionElement.appendChild(sectionElement);

        List<TableRow> rows = division.getRows()
            .stream()
            .filter(t -> t.getSection() == null)
            .collect(toList());

        List<Element> rowsElement = createRowElements(doc, rows, context);
        for (Element rowElement : rowsElement)
            divisionElement.appendChild(rowElement);

        return divisionElement;
    }


    private List<Element> createSectionElements(Document doc, List<TableSection> sections, CalcContext context) {
        return sections.stream()
            .sorted(Comparator.comparing(TableSection::getOrderNum))
            .map(section -> createSectionElement(doc, section, context))
            .collect(toList());
    }

    private Element createSectionElement(Document doc, TableSection section, CalcContext context) {
        Element sectionElement = doc.createElement("section");
        sectionElement.setAttribute("name", section.getName());
        sectionElement.setAttribute("is-total", section.getHasTotal().toString().toLowerCase());
        sectionElement.setAttribute("is-title", section.getHasTitle().toString().toLowerCase());

        List<Element> rowElements = createRowElements(doc, section.getRows(), context);
        for (Element rowElement : rowElements)
            sectionElement.appendChild(rowElement);

        return sectionElement;
    }


    private List<Element> createRowElements(Document doc, List<TableRow> rows, CalcContext context) {
        return rows.stream()
            .sorted(Comparator.comparing(TableRow::getOrderNum))
            .map(row -> createRowElement(doc, row, context))
            .collect(toList());
    }

    private Element createRowElement(Document doc, TableRow row, CalcContext context) {
        Element rowElement;
        if (!row.getIsTotal()) {
            rowElement = doc.createElement("row");
            rowElement.setAttribute("is-total", "true");
        }
        else
            rowElement = doc.createElement("total");

        List<Element> cellElements = createCellElements(doc, row.getCells(), context);
        for (Element cellElement : cellElements)
            rowElement.appendChild(cellElement);

        return rowElement;
    }

    private List<Element> createCellElements(Document doc, List<TableCell> cells, CalcContext context) {
        return cells.stream()
            .sorted(Comparator.comparing(c -> c.getAttr().getOrderNum()))
            .map(cell -> createCellElement(doc, cell, context))
            .collect(toList());
    }

    private Element createCellElement(Document doc, TableCell cell, CalcContext context) {
        if (cell.getRow().getIsTotal())
            return createTotalCellElement(doc, cell, context);

        cell = calc(cell, context);

        AttrTypeEnum attrType = cell.getAttrType();
        if (attrType==null)
            attrType = cell.getAttr().getAttrType();

        Long precision = cell.getPrecision();
        if (precision == null)
            precision = cell.getAttr().getPrecision();

        Element attrElement = doc.createElement("attr");

        if (attrType!=null)
            attrElement.setAttribute("type", attrType.toString().toLowerCase());

        if (precision!=null)
            attrElement.setAttribute("precision", precision.toString());

        if (cell.getVal() != null)
            attrElement.appendChild(doc.createTextNode(cell.getVal()));

        return attrElement;
    }

    private Element createTotalCellElement(Document doc, TableCell cell, CalcContext context) {
        cell = calc(cell, context);

        Element attrElement = doc.createElement("attr");

        AttrTypeEnum attrType = cell.getAttrType();
        if (attrType==null)
            attrType = cell.getAttr().getAttrType();

        Long precision = cell.getPrecision();
        if (precision == null)
            precision = cell.getAttr().getPrecision();

        attrElement.setAttribute("type", "empty");
        if (cell.getVal() != null && attrType!=null) {
            attrElement.setAttribute("name", cell.getAttr().getName());
            attrElement.setAttribute("type", attrType.toString().toLowerCase());
            attrElement.appendChild(doc.createTextNode(cell.getVal()));
        }

        if (precision!=null)
            attrElement.setAttribute("precision", precision.toString());

        return attrElement;
    }

    private TableCell calc(TableCell cell, CalcContext context)  {
        if (cell.getFormula()!=null) {
            CalcResult result = null;
            try {
                result = calcService.calc(cell.getFormula(), context);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            if (result.getVal()!=null)
                cell.setVal(result.getVal().toString());
        }

        return cell;
    }
}
