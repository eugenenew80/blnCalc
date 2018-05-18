package calc;

import calc.entity.rep.*;
import calc.formula.CalcContext;
import calc.rep.ReportBuilder;
import calc.rep.ReportExecutorService;
import calc.repo.rep.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;

@EntityScan(
    basePackageClasses = { App.class, Jsr310JpaConverters.class }
)
@SpringBootApplication
public class App  {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Autowired
    private ReportExecutorService executorService;

    @Autowired

    private ReportBuilder reportBuilder;
    @Autowired
    private ReportRepo reportRepo;

    @Autowired
    private RowTemplateRepo rowTemplateRepo;

    @Autowired
    private DivisionRepo divisionRepo;

    @Autowired
    private SectionRepo sectionRepo;

    @Autowired
    private RowRepo rowRepo;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }


    @PostConstruct
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void init() throws Exception {
        Report templateReport = reportRepo.findByCodeAndIsTemplateIsTrue("ACT-SUBST");

        Report report = reportRepo.findByCode("ACT-SUBST#SHYM-500");
        if (report==null)
            report = createActReport(templateReport.getCode(), "ACT-SUBST#SHYM-500");

        buildReport(report.getId());
    }

    private void buildReport(Long reportId) throws Exception {
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

        Report report = reportRepo.findOne(reportId);
        Document document = executorService.buildReport(report.getId(), context);
        executorService.save(document, "files/doc.xml");

        Document doc = executorService.transform(document);
        executorService.save(doc, "files/report.xml");
    }

    private Report createActReport(String reportCode, String newReportCode) {
        Report report = reportBuilder.createFromTemplate(reportCode, newReportCode);
        Map<String, List<Pair<String, String>>> mapParams = createParams();

        report.getSections()
            .stream()
            .forEach(t -> {
                List<Pair<String, String>> params = mapParams.get(t.getCode());
                if (params!=null)
                    reportBuilder.createSectionRows(t, params);
            });

        reportBuilder.createTotals(report);
        return reportRepo.findOne(report.getId());
    }

    private Map<String, List<Pair<String, String>>> createParams() {
        Map<String, List<Pair<String, String>>>  mapParams = new HashMap<>();

        List<Pair<String, String>> params = Arrays.asList(
            Pair.of("121420300070120001", "A-"),
            Pair.of("121420300070120002", "A-")
        );
        mapParams.put("1.1", params);


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
        mapParams.put("1.2", params);


        params = Arrays.asList(
            Pair.of("121420300070120015", "A-"),
            Pair.of("121420300070120016", "A-")
        );
        mapParams.put("1.3", params);


        params = Arrays.asList(
            Pair.of("121420300070120001", "A+"),
            Pair.of("121420300070120002", "A+")
        );
        mapParams.put("2.1", params);


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
        mapParams.put("2.2", params);


        params = Arrays.asList(
            Pair.of("121420300070120030", "A+"),
            Pair.of("121420300070120031", "A+"),
            Pair.of("121420300070120018", "A+"),
            Pair.of("121420300070120033", "A+")
        );
        mapParams.put("2.3", params);


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
        mapParams.put("2.4", params);


        params = Arrays.asList(
            Pair.of("121420300070120035", "A+"),
            Pair.of("121420300070120036", "A+"),
            Pair.of("121420300070120037", "A+"),
            Pair.of("121420300070120038", "A+")
        );
        mapParams.put("2.5", params);


        params = Arrays.asList(
            Pair.of("121420300070120019", "A-"),
            Pair.of("121420300070120020", "A-"),
            Pair.of("121420300070120027", "A-"),
            Pair.of("121420300070120028", "A-")
        );
        mapParams.put("3.1", params);


        params = Arrays.asList(
            Pair.of("121420300070120024", "A-"),
            Pair.of("121420300070120025", "A-")
        );
        mapParams.put("3.2", params);


        params = Arrays.asList(
            Pair.of("121420300070120026", "A-")
        );
        mapParams.put("3.3", params);

        return mapParams;
    }
}
