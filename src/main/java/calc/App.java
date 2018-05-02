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
import java.io.File;
import java.util.Comparator;
import java.util.List;

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
                                        Element rowElement;
                                        if (!row.getIsTotal()) {
                                            rowElement = doc.createElement("row");
                                            rowElement.setAttribute("is-total", "true");
                                        }
                                        else
                                            rowElement = doc.createElement("total");

                                        sectionElement.appendChild(rowElement);

                                        List<ReportCell> cells = row.getCells();
                                        for (ReportCell cell : cells) {
                                            Element attrElement = doc.createElement("attr");
                                            attrElement.setAttribute("name", cell.getAttr().getName());
                                            attrElement.setAttribute("type", cell.getAttr().getAttrType().toString().toLowerCase());
                                            attrElement.setNodeValue(cell.getVal());
                                            rowElement.appendChild(attrElement);
                                        }
                                }) ;
                        });
                    });
            }
        }

        TransformerFactory tranFactory = TransformerFactory.newInstance();
        Transformer aTransformer = tranFactory.newTransformer();
        Source src = new DOMSource(doc);
        Result dest = new StreamResult(new File("files/xmlFileName.xml"));
        aTransformer.transform(src, dest);
    }

    @Autowired
    private ReportRepo reportRepo;
}
