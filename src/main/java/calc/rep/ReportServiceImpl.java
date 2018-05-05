package calc.rep;

import calc.entity.rep.*;
import calc.entity.rep.enums.AttrTypeEnum;
import calc.entity.rep.enums.TablePartEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
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
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportRepo reportRepo;
    private final CalcService calcService;

    public ReportCell calc(ReportCell cell)  {
        CalcContext context = CalcContext.builder()
            .startDate(LocalDate.of(2018, 3, 1))
            .endDate(LocalDate.of(2018, 3, 31))
            .orgId(11l)
            .build();

        context.setValues(new ArrayList<>());
        context.setTrace(new HashMap<>());

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

    @Override
    public Document buildReport(Long reportId) throws Exception {
        Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .newDocument();

        Report report = reportRepo.findOne(reportId);
        Document result = createReportElement(report, doc);
        save(result, "files/doc.xml");

        return result;
    }

    private void save(Document doc, String fileName) throws Exception {
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

        DOMResult output = new DOMResult();
        trans.transform(new DOMSource(doc), output);

        return (Document) output.getNode();
    }

    @Override
    public Document transform(Long reportId, Source xslSource) throws Exception {
        return transform(
            buildReport(reportId),
            xslSource
        );
    }

    private Document createReportElement(Report report, Document doc) throws Exception {
        Element reportElement = doc.createElement("report");
        reportElement.setAttribute("type", report.getReportType());

        List<Element> sheetElements = createSheetElements(doc, report.getSheets());
        for (Element sheetElement : sheetElements)
            reportElement.appendChild(sheetElement);

        doc.appendChild(reportElement);
        return doc;
    }

    private List<Element> createSheetElements(Document doc, List<ReportSheet> sheets) {
        return sheets.stream()
            .map(sheet -> createSheetElement(doc, sheet))
            .collect(toList());
    }

    private Element createSheetElement(Document doc, ReportSheet sheet) {
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

        Element sheetHeaderElement = createSheetHeaderElement(doc);
        sheetElement.appendChild(sheetHeaderElement);

        List<Element> tableElements = createTableElements(doc, sheet.getTables());
        for (Element tableElement : tableElements)
            sheetElement.appendChild(tableElement);

        return sheetElement;
    }

    private Element createSheetHeaderElement(Document doc) {
        Element headElement = doc.createElement("head");

        Element repNameElement = doc.createElement("name");
        repNameElement.appendChild(doc.createTextNode("АКТ"));
        headElement.appendChild(repNameElement);

        Element repPeriodElement = doc.createElement("period");
        repPeriodElement.setAttribute("start-date", "2018-03-01");
        repPeriodElement.setAttribute("end-date", "2018-03-31");
        headElement.appendChild(repPeriodElement);

        Element repEnergyObjectElement = doc.createElement("energy-object");
        repEnergyObjectElement.setAttribute("type", "subst");
        repEnergyObjectElement.setAttribute("name", "ПС 500 кВ Шымкент");
        headElement.appendChild(repEnergyObjectElement);

        return headElement;
    }

    private List<Element> createTableElements(Document doc, List<ReportTable> tables) {
        return tables.stream()
            .sorted(Comparator.comparing(ReportTable::getId))
            .map(table -> createTableElement(doc, table))
            .collect(toList());
    }

    private Element createTableElement(Document doc, ReportTable table) {
        Element tableElement = doc.createElement("table");
        tableElement.setAttribute("name", table.getName());

        if (table.getHasHeader()) {
            Element tableHeadElement = createTableHeaderElement(doc, table);
            tableElement.appendChild(tableHeadElement);

            Element bodyElement = doc.createElement("body");
            tableElement.appendChild(bodyElement);

            List<ReportDivision> divisions = table.getDivisions()
                .stream()
                .filter(t -> t.getBelongTo() == TablePartEnum.BODY)
                .collect(toList());

            List<Element> divisionElements = createDivisionElements(doc, divisions);
            for (Element divisionElement : divisionElements)
                bodyElement.appendChild(divisionElement);

            Element footerElement = doc.createElement("footer");
            tableElement.appendChild(footerElement);

            divisions = table.getDivisions()
                .stream()
                .filter(t -> t.getBelongTo() == TablePartEnum.FOOTER)
                .collect(toList());

            divisionElements = createDivisionElements(doc, divisions);
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
            .sorted(Comparator.comparing(ReportAttr::getOrderNum))
            .forEach(attr -> {
                Element tableColumnElement = doc.createElement("column");
                tableColumnElement.setAttribute("type", attr.getAttrType().toString().toLowerCase());
                tableColumnElement.setAttribute("attr", attr.getName());
                tableColumnElement.setAttribute("name", attr.getDescription());
                tableHeadElement.appendChild(tableColumnElement);
            });

        return tableHeadElement;
    }


    private List<Element> createDivisionElements(Document doc, List<ReportDivision> divisions) {
        return divisions.stream()
            .sorted(Comparator.comparing(ReportDivision::getOrderNum))
            .map(division -> createDivisionElement(doc, division))
            .collect(toList());
    }

    private Element createDivisionElement(Document doc, ReportDivision division) {
        Element divisionElement = doc.createElement("division");
        divisionElement.setAttribute("name", division.getName());
        divisionElement.setAttribute("is-total", division.getHasTotal().toString().toLowerCase());
        divisionElement.setAttribute("is-title", division.getHasTitle().toString().toLowerCase());

        List<Element> sectionElements = createSectionElements(doc, division.getSections());
        for (Element sectionElement : sectionElements)
            divisionElement.appendChild(sectionElement);

        List<ReportRow> rows = division.getRows()
            .stream()
            .filter(t -> t.getSection() == null)
            .collect(toList());

        List<Element> rowsElement = createRowElements(doc, rows);
        for (Element rowElement : rowsElement)
            divisionElement.appendChild(rowElement);

        return divisionElement;
    }


    private List<Element> createSectionElements(Document doc, List<ReportSection> sections) {
        return sections.stream()
            .sorted(Comparator.comparing(ReportSection::getOrderNum))
            .map(section -> createSectionElement(doc, section))
            .collect(toList());
    }

    private Element createSectionElement(Document doc, ReportSection section) {
        Element sectionElement = doc.createElement("section");
        sectionElement.setAttribute("name", section.getName());
        sectionElement.setAttribute("is-total", section.getHasTotal().toString().toLowerCase());
        sectionElement.setAttribute("is-title", section.getHasTitle().toString().toLowerCase());

        List<Element> rowElements = createRowElements(doc, section.getRows());
        for (Element rowElement : rowElements)
            sectionElement.appendChild(rowElement);

        return sectionElement;
    }


    private List<Element> createRowElements(Document doc, List<ReportRow> rows) {
        return rows.stream()
            .sorted(Comparator.comparing(ReportRow::getOrderNum))
            .map(row -> createRowElement(doc, row))
            .collect(toList());
    }

    private Element createRowElement(Document doc, ReportRow row) {
        Element rowElement;
        if (!row.getIsTotal()) {
            rowElement = doc.createElement("row");
            rowElement.setAttribute("is-total", "true");
        }
        else
            rowElement = doc.createElement("total");

        List<Element> cellElements = createCellElements(doc, row.getCells());
        for (Element cellElement : cellElements)
            rowElement.appendChild(cellElement);

        return rowElement;
    }

    private List<Element> createCellElements(Document doc, List<ReportCell> cells) {
        return cells.stream()
            .sorted(Comparator.comparing(c -> c.getAttr().getOrderNum()))
            .map(cell -> createCellElement(doc, cell))
            .collect(toList());
    }

    private Element createCellElement(Document doc, ReportCell cell) {
        if (cell.getRow().getIsTotal())
            return createTotalCellElement(doc, cell);

        cell = calc(cell);

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

    private Element createTotalCellElement(Document doc, ReportCell cell) {
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
}
