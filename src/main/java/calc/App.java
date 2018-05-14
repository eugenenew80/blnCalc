package calc;

import calc.formula.CalcContext;
import calc.rep.ReportBuilder;
import calc.rep.TemplateReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.w3c.dom.Document;
import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
        CalcContext context = CalcContext.builder()
            .startDate(LocalDate.of(2018, 3, 1))
            .endDate(LocalDate.of(2018, 3, 31))
            .orgId(11l)
            .orgName("Южные МЭС")
            .reportName("АКТ")
            .energyObjectType("SUBST")
            .energyObjectId(11l)
            .energyObjectName("ПС Шымкент 500")
            .trace(new HashMap<>())
            .values(new ArrayList<>())
            .build();

        Document document = reportService.buildReport(22l, context);
        reportService.save(document, "files/doc.xml");

        Document doc = reportService.transform(document);
        reportService.save(doc, "files/report.xml");

        //reportService.createFromTemplate(1l);

        /*
        reportBuilder.createRows(32l, Arrays.asList(
            "121420300070120001",
            "121420300070120002"
        ));

        reportBuilder.createRows(33l, Arrays.asList(
            "121420300070120004",
            "121420300070120005",
            "121420300070120007",
            "121420300070120009",
            "121420300070120010",
            "121420300070120012",
            "121420300070120013",
            "121420300070120014",
            "121420300070120003",
            "121420300070120029"
        ));

        reportBuilder.createRows(34l, Arrays.asList(
            "121420300070120015",
            "121420300070120016"
        ));

        reportBuilder.createRows(35l, Arrays.asList(
            "121420300070120001",
            "121420300070120002"
        ));

        reportBuilder.createRows(36l, Arrays.asList(
            "121420300070120004",
            "121420300070120005",
            "121420300070120006",
            "121420300070120007",
            "121420300070120008",
            "121420300070120009",
            "121420300070120010",
            "121420300070120011",
            "121420300070120012",
            "121420300070120013",
            "121420300070120014",
            "121420300070120003",
            "121420300070120029"
        ));

        reportBuilder.createRows(37l, Arrays.asList(
            "121420300070120030",
            "121420300070120031",
            "121420300070120018",
            "121420300070120033"
        ));

        reportBuilder.createRows(38l, Arrays.asList(
            "121420300070120021",
            "121420300070120022",
            "121420300070120023",
            "121420300070120039",
            "121420300070120040",
            "121420300070120041",
            "121420300070120042",
            "121420300070120043",
            "121420300070120044"
        ));

        reportBuilder.createRows(39l, Arrays.asList(
            "121420300070120035",
            "121420300070120036",
            "121420300070120037",
            "121420300070120038"
        ));

        reportBuilder.createRows(40l, Arrays.asList(
            "121420300070120019",
            "121420300070120020",
            "121420300070120027",
            "121420300070120028"
        ));

        reportBuilder.createRows(41l, Arrays.asList(
            "121420300070120024",
            "121420300070120025"
        ));

        reportBuilder.createRows(42l, Arrays.asList(
            "121420300070120026"
        ));
        */

        //reportBuilder.createCells(32l, "A-");
        //reportBuilder.createCells(33l, "A-");
        //reportBuilder.createCells(34l, "A-");

        //reportBuilder.createCells(35l, "A+");
        //reportBuilder.createCells(36l, "A+");
        //reportBuilder.createCells(37l, "A+");
        //reportBuilder.createCells(38l, "A+");
        //reportBuilder.createCells(39l, "A+");

        //reportBuilder.createCells(40l, "A-");
        //reportBuilder.createCells(41l, "A-");
        //reportBuilder.createCells(42l, "A-");
    }

    @Autowired
    private TemplateReportBuilder reportService;

    @Autowired
    private ReportBuilder reportBuilder;
}
