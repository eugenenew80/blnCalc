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

@EntityScan(
    basePackageClasses = { App.class, Jsr310JpaConverters.class }
)
@SpringBootApplication
public class App  {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final String XSL_FILE = "files/report.xsl";
    private static final String INPUT_FILE = "files/doc.xml";
    private static final String OUTPUT_FILE = "files/excel.xml";

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @PostConstruct
    public void init() throws Exception {
        buildReport();
    }

    public void buildReport() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Report report = reportRepo.findOne(1l);
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("report");
        rootElement.setAttribute("type", report.getReportType());

        doc.appendChild(rootElement);

        List<ReportSheet> sheets = report.getSheets();
        for (ReportSheet sheet : sheets) {

            Element sheetElement = doc.createElement("sheet");
            sheetElement.setAttribute("name", sheet.getName());
            rootElement.appendChild(sheetElement);

            sheet.getColumns()
                .stream()
                .sorted(Comparator.comparing(SheetColumn::getOrderNum))
                .forEach(column -> {
                    Element columnElement = doc.createElement("column");
                    columnElement.setAttribute("width", column.getWidth().toString());
                    sheetElement.appendChild(columnElement);
                });

            Element headElement = doc.createElement("head");
            sheetElement.appendChild(headElement);

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

            List<ReportTable> tables = sheet.getTables();
            for (ReportTable table : tables) {
                Element tableElement = doc.createElement("table");
                tableElement.setAttribute("name", table.getName());
                sheetElement.appendChild(tableElement);

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


                Element bodyElement = doc.createElement("body");
                tableElement.appendChild(bodyElement);

                table.getDivisions()
                    .stream()
                    .sorted(Comparator.comparing(ReportDivision::getOrderNum))
                    .forEach(division -> {
                        Element divisionElement = doc.createElement("division");
                        divisionElement.setAttribute("name", division.getName());
                        divisionElement.setAttribute("is-total", division.getHasTotal().toString().toLowerCase());
                        divisionElement.setAttribute("is-title", division.getHasTitle().toString().toLowerCase());
                        bodyElement.appendChild(divisionElement);

                        division.getSections()
                            .stream()
                            .sorted(Comparator.comparing(ReportSection::getOrderNum))
                            .forEach(section -> {
                                Element sectionElement = doc.createElement("section");
                                sectionElement.setAttribute("name", section.getName());
                                sectionElement.setAttribute("is-total", section.getHasTotal().toString().toLowerCase());
                                sectionElement.setAttribute("is-title", section.getHasTitle().toString().toLowerCase());
                                divisionElement.appendChild(sectionElement);

                                section.getRows()
                                    .stream()
                                    .sorted(Comparator.comparing(ReportRow::getOrderNum))
                                    .forEach(row -> {
                                        Element rowElement = createRowElement(doc, row);
                                        sectionElement.appendChild(rowElement);
                                });
                        });

                        division.getRows()
                            .stream()
                            .filter(t -> t.getSection()==null)
                            .sorted(Comparator.comparing(ReportRow::getOrderNum))
                            .forEach(row -> {
                                Element rowElement = createRowElement(doc, row);
                                divisionElement.appendChild(rowElement);
                            });
                    });
            }
        }

        TransformerFactory tranFactory = TransformerFactory.newInstance();
        Transformer aTransformer = tranFactory.newTransformer();
        Source src = new DOMSource(doc);
        Result dest = new StreamResult(new File(INPUT_FILE));
        aTransformer.transform(src, dest);


        StreamSource xslCode = new StreamSource(new File(XSL_FILE));
        StreamSource input = new StreamSource(new File(INPUT_FILE));
        StreamResult output = new StreamResult(new File(OUTPUT_FILE));

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer(xslCode);

        trans.transform(input, output);
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
