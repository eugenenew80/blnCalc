package calc;

import calc.entity.rep.Report;
import calc.entity.rep.ReportTable;
import calc.entity.rep.TableSection;
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
import org.springframework.data.util.Pair;
import org.w3c.dom.Document;
import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        Report report = templateReportBuilder.createFromTemplate(1l);

        for (TableSection section : report.getSections()) {
            List<Pair<String, String>> params = null;

            if (section.getCode()!=null && section.getCode().equals("1.1")) {
                params = Arrays.asList(
                    Pair.of("121420300070120001", "A-"),
                    Pair.of("121420300070120002", "A-")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("1.2")) {
                params = Arrays.asList(
                    Pair.of("121420300070120004", "A-"),
                    Pair.of("121420300070120005", "A-"),
                    Pair.of("121420300070120007", "A-"),
                    Pair.of("121420300070120009", "A-"),
                    Pair.of("121420300070120010", "A-"),
                    Pair.of("121420300070120012", "A-"),
                    Pair.of("121420300070120013", "A-"),
                    Pair.of("121420300070120014", "A-"),
                    Pair.of("121420300070120003", "A-"),
                    Pair.of("121420300070120029", "A-")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("1.3")) {
                params = Arrays.asList(
                    Pair.of("121420300070120015", "A-"),
                    Pair.of("121420300070120016", "A-")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("2.1")) {
                params = Arrays.asList(
                    Pair.of("121420300070120001", "A+"),
                    Pair.of("121420300070120002", "A+")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("2.2")) {
                params = Arrays.asList(
                    Pair.of("121420300070120004", "A+"),
                    Pair.of("121420300070120005", "A+"),
                    Pair.of("121420300070120006", "A+"),
                    Pair.of("121420300070120007", "A+"),
                    Pair.of("121420300070120008", "A+"),
                    Pair.of("121420300070120009", "A+"),
                    Pair.of("121420300070120010", "A+"),
                    Pair.of("121420300070120011", "A+"),
                    Pair.of("121420300070120012", "A+"),
                    Pair.of("121420300070120013", "A+"),
                    Pair.of("121420300070120014", "A+"),
                    Pair.of("121420300070120003", "A+"),
                    Pair.of("121420300070120029", "A+")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("2.3")) {
                params = Arrays.asList(
                    Pair.of("121420300070120030", "A+"),
                    Pair.of("121420300070120031", "A+"),
                    Pair.of("121420300070120018", "A+"),
                    Pair.of("121420300070120033", "A+")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("2.4")) {
                params = Arrays.asList(
                    Pair.of("121420300070120021", "A+"),
                    Pair.of("121420300070120022", "A+"),
                    Pair.of("121420300070120023", "A+"),
                    Pair.of("121420300070120039", "A+"),
                    Pair.of("121420300070120040", "A+"),
                    Pair.of("121420300070120041", "A+"),
                    Pair.of("121420300070120042", "A+"),
                    Pair.of("121420300070120043", "A+"),
                    Pair.of("121420300070120044", "A+")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("2.5")) {
                params = Arrays.asList(
                    Pair.of("121420300070120035", "A+"),
                    Pair.of("121420300070120036", "A+"),
                    Pair.of("121420300070120037", "A+"),
                    Pair.of("121420300070120038", "A+")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("3.1")) {
                params = Arrays.asList(
                    Pair.of("121420300070120019", "A-"),
                    Pair.of("121420300070120020", "A-"),
                    Pair.of("121420300070120027", "A-"),
                    Pair.of("121420300070120028", "A-")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("3.2")) {
                params = Arrays.asList(
                    Pair.of("121420300070120024", "A-"),
                    Pair.of("121420300070120025", "A-")
                );
            }

            if (section.getCode()!=null && section.getCode().equals("3.3")) {
                params = Arrays.asList(
                    Pair.of("121420300070120026", "A-")
                );
            }

            reportBuilder.generateSectionRows(section.getId(), params);
        }

        for (ReportTable table : report.getTables())
            reportBuilder.generateTotals(table.getId());


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

        Document document = templateReportBuilder.buildReport(22l, context);
        templateReportBuilder.save(document, "files/doc.xml");

        Document doc = templateReportBuilder.transform(document);
        templateReportBuilder.save(doc, "files/report.xml");
    }


    @Autowired
    private TemplateReportBuilder templateReportBuilder;

    @Autowired
    private ReportBuilder reportBuilder;
}
