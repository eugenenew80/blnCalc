package calc;

import calc.entity.rep.*;
import calc.repo.rep.ReportRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.*;

@EntityScan(
    basePackageClasses = { App.class, Jsr310JpaConverters.class }
)
@SpringBootApplication
public class App  {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @PostConstruct
    public void init() throws Exception {
        buildReport();
    }

    public void buildReport() throws Exception {
        Report report = reportRepo.findOne(1l);
        Document doc = createReportElement(report);

        StreamSource xslCode = new StreamSource(new File("files/report.xsl"));
        StreamResult output = new StreamResult(new File("files/excel.xml"));
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer(xslCode);
        trans.transform(new DOMSource(doc), output);
    }

    private Document createReportElement(Report report) throws Exception {
        Document doc = createDocument();

        Element rootElement = doc.createElement("report");
        rootElement.setAttribute("type", report.getReportType());
        doc.appendChild(rootElement);
        createSheetsElement(doc, rootElement, report.getSheets());

        return doc;
    }

    private Document createDocument() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    private void createSheetsElement(Document doc, Element parentElement, List<ReportSheet> sheets) {
        sheets.stream()
            .forEach(sheet -> {
                Element sheetElement = doc.createElement("sheet");
                sheetElement.setAttribute("name", sheet.getName());
                parentElement.appendChild(sheetElement);
                sheet.getColumns()
                    .stream()
                    .sorted(Comparator.comparing(SheetColumn::getOrderNum))
                    .forEach(column -> {
                        Element columnElement = doc.createElement("column");
                        columnElement.setAttribute("width", column.getWidth().toString());
                        sheetElement.appendChild(columnElement);
                    });

                sheetElement.appendChild(createHeaderElement(doc));

                createTablesElement(doc, sheetElement, sheet.getTables());
            });
    }

    private void createTablesElement(Document doc, Element parentElement, List<ReportTable> tables) {
        tables.stream()
            .sorted(Comparator.comparing(ReportTable::getId))
            .forEach(table -> {
                Element tableElement = createTableElement(doc, table);
                parentElement.appendChild(tableElement);
                Element bodyElement = doc.createElement("body");
                tableElement.appendChild(bodyElement);

                createDivisionsElement(doc, bodyElement, table.getDivisions());
            });
    }

    private void createDivisionsElement(Document doc, Element parentElement, List<ReportDivision> divisions) {
        divisions.stream()
            .sorted(Comparator.comparing(ReportDivision::getOrderNum))
            .forEach(division -> {
                Element divisionElement = createDivisionElement(doc, division);
                parentElement.appendChild(divisionElement);

                createSectionsElement(doc, divisionElement, division.getSections());

                List<ReportRow> rows = division.getRows()
                    .stream()
                    .filter(t -> t.getSection() == null)
                    .collect(toList());

                createRowsElement(doc, divisionElement, rows);
            });
    }

    private void createSectionsElement(Document doc, Element parentElement, List<ReportSection> sections) {
        sections.stream()
            .sorted(Comparator.comparing(ReportSection::getOrderNum))
            .forEach(section -> {
                Element sectionElement = createSectionElement(doc, section);
                parentElement.appendChild(sectionElement);
                createRowsElement(doc, sectionElement, section.getRows());
            });
    }

    private void createRowsElement(Document doc, Element parentElement, List<ReportRow> rows) {
        rows.stream()
            .sorted(Comparator.comparing(ReportRow::getOrderNum))
            .forEach(row -> {
                Element rowElement = createRowElement(doc, row);
                parentElement.appendChild(rowElement);
            });
    }

    private Element createSectionElement(Document doc, ReportSection section) {
        Element sectionElement = doc.createElement("section");
        sectionElement.setAttribute("name", section.getName());
        sectionElement.setAttribute("is-total", section.getHasTotal().toString().toLowerCase());
        sectionElement.setAttribute("is-title", section.getHasTitle().toString().toLowerCase());
        return sectionElement;
    }

    private Element createDivisionElement(Document doc, ReportDivision division) {
        Element divisionElement = doc.createElement("division");
        divisionElement.setAttribute("name", division.getName());
        divisionElement.setAttribute("is-total", division.getHasTotal().toString().toLowerCase());
        divisionElement.setAttribute("is-title", division.getHasTitle().toString().toLowerCase());
        return divisionElement;
    }

    private Element createTableElement(Document doc, ReportTable table) {
        Element tableElement = doc.createElement("table");
        tableElement.setAttribute("name", table.getName());

        if (table.getHasHeader()) {
            Element tableHeadElement = doc.createElement("head");
            table.getAttrs()
                .stream()
                .sorted(Comparator.comparing(ReportAttr::getOrderNum))
                .forEach(attr -> {
                    Element tableColumnElement = doc.createElement("column");
                    tableColumnElement.setAttribute("type", attr.getAttrType().toString().toLowerCase());
                    tableColumnElement.setAttribute("attr", attr.getName());
                    tableColumnElement.setAttribute("name", attr.getDescription());
                    tableHeadElement.appendChild(tableColumnElement);
                });
            tableElement.appendChild(tableHeadElement);
        }
        return tableElement;
    }

    private Element createHeaderElement(Document doc) {
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

    private Element createRowElement(Document doc, ReportRow row) {
        Element rowElement;
        if (!row.getIsTotal()) {
            rowElement = doc.createElement("row");
            rowElement.setAttribute("is-total", "true");
        }
        else
            rowElement = doc.createElement("total");

        row.getCells()
            .stream()
            .sorted(Comparator.comparing(c -> c.getAttr().getOrderNum()))
            .forEach(cell -> {
                Element attrElement = doc.createElement("attr");

                if (!row.getIsTotal() ) {
                    attrElement.setAttribute("type", cell.getAttr().getAttrType().toString().toLowerCase());
                }
                else
                    if (cell.getVal() != null) {
                        attrElement.setAttribute("name", cell.getAttr().getName());
                        attrElement.setAttribute("type", cell.getAttr().getAttrType().toString().toLowerCase());
                    }
                    else
                        attrElement.setAttribute("type", "empty");

                if (cell.getAttr().getPrecision()!=null)
                    attrElement.setAttribute("precision", cell.getAttr().getPrecision().toString());

                rowElement.appendChild(attrElement);
                if (cell.getVal() != null)
                    attrElement.appendChild(doc.createTextNode(cell.getVal()));
            });
        return rowElement;
    }

    @Autowired
    private ReportRepo reportRepo;
}
