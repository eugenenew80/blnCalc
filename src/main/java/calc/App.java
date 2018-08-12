package calc;

import calc.entity.rep.*;
import calc.formula.CalcContext;
import calc.rep.ReportBuilder;
import calc.rep.ReportExecutorService;
import calc.repo.calc.MeteringPointRepo;
import calc.repo.rep.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@EntityScan(
    basePackageClasses = { App.class, Jsr310JpaConverters.class }
)
@SpringBootApplication
@EnableScheduling
public class App  {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Autowired
    private ReportExecutorService executorService;

    @Autowired
    private ReportBuilder reportBuilder;

    @Autowired
    private ReportRepo reportRepo;

    @Autowired
    private MeteringPointRepo meteringPointRepo;

    @Autowired
    private GroupHeaderRepo groupHeaderRepo;

    @Autowired
    private GroupLineRepo groupLineRepo;

    @Autowired
    private TableGroupHeaderRepo tableGroupHeaderRepo;

    @Autowired
    private RowRepo rowRepo;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    //@PostConstruct
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void init() throws Exception {
        Report templateReport = reportRepo.findByCodeAndIsTemplateIsTrue("ACT-SUBST");

        Report report = reportRepo.findByCode("ACT-SUBST#SHYM-500");
        if (report==null) {
            createGroupHeaders();
            report = createActReport(templateReport.getCode(), "ACT-SUBST#SHYM-500");
        }

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
        List<TableGroupHeader> tableGroupHeaders = createTableGroupHeaders(report);

        tableGroupHeaders.stream().forEach(header -> {
            List<Pair<String, String>> params = header.getGroupHeader()
                .getLines()
                .stream()
                .map(line -> Pair.of(line.getMeteringPoint().getCode(), header.getParamCode()))
                .collect(Collectors.toList());

            List<TableRow> rows = reportBuilder.createSectionRows(header.getSection(), params);
            rowRepo.save(rows);
        });

        List<TableRow> rows = reportBuilder.createSectionTotals(report);
        rowRepo.save(rows);

        rows = reportBuilder.createDivisionTotals(report);
        rowRepo.save(rows);

        return reportRepo.findOne(report.getId());
    }

    private List<TableGroupHeader> createTableGroupHeaders(Report report) {
        List<TableGroupHeader> tableGroupHeaders = report.getSheets()
            .stream()
            .flatMap(t -> t.getTables().stream())
            .flatMap(t -> t.getDivisions().stream())
            .flatMap(t -> t.getSections().stream())
            .map(section -> {
                String paramCode = "";
                Long groupHeaderId = null;
                switch (section.getCode()) {
                    case "1.1":
                        paramCode = "A-";
                        groupHeaderId = 1l;
                        break;
                    case "1.2":
                        paramCode = "A-";
                        groupHeaderId = 2l;
                        break;
                    case "1.3":
                        paramCode = "A-";
                        groupHeaderId = 3l;
                        break;
                    case "2.1":
                        paramCode = "A+";
                        groupHeaderId = 4l;
                        break;
                    case "2.2":
                        paramCode = "A+";
                        groupHeaderId = 5l;
                        break;
                    case "2.3":
                        paramCode = "A+";
                        groupHeaderId = 6l;
                        break;
                    case "2.4":
                        paramCode = "A+";
                        groupHeaderId = 7l;
                        break;
                    case "2.5":
                        paramCode = "A+";
                        groupHeaderId = 8l;
                        break;
                    case "3.1":
                        paramCode = "A-";
                        groupHeaderId = 9l;
                        break;
                    case "3.2":
                        paramCode = "A-";
                        groupHeaderId = 10l;
                        break;
                    case "3.3":
                        paramCode = "A-";
                        groupHeaderId = 11l;
                        break;
                }
                GroupHeader groupHeader = groupHeaderRepo.findOne(groupHeaderId);

                TableGroupHeader tableGroupHeader = new TableGroupHeader();
                tableGroupHeader.setGroupHeader(groupHeader);
                tableGroupHeader.setSection(section);
                tableGroupHeader.setDivision(section.getDivision());
                tableGroupHeader.setReport(section.getReport());
                tableGroupHeader.setParamCode(paramCode);
                return tableGroupHeader;
            })
            .collect(Collectors.toList());

        tableGroupHeaderRepo.save(tableGroupHeaders);
        return tableGroupHeaders;
    }

    private void createGroupHeaders() {
        GroupHeader header = new GroupHeader();
        header.setName("Шины 500 кВ - приём");

        List<GroupLine> lines = new ArrayList<>();
        GroupLine line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120001"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120002"));
        line.setOrderNum(2l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 220 кВ - приём");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120004"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120005"));
        line.setOrderNum(2l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120009"));
        line.setOrderNum(3l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120010"));
        line.setOrderNum(4l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120012"));
        line.setOrderNum(5l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120013"));
        line.setOrderNum(6l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120014"));
        line.setOrderNum(7l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120003"));
        line.setOrderNum(8l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120029"));
        line.setOrderNum(9l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 220 кВ Ввода");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120015"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120016"));
        line.setOrderNum(2l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 500 кВ - отдача");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120001"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120002"));
        line.setOrderNum(2l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 220 кВ - отдача");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120004"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120005"));
        line.setOrderNum(2l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120006"));
        line.setOrderNum(3l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120007"));
        line.setOrderNum(4l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120008"));
        line.setOrderNum(5l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120009"));
        line.setOrderNum(6l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120010"));
        line.setOrderNum(7l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120011"));
        line.setOrderNum(8l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120012"));
        line.setOrderNum(9l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120013"));
        line.setOrderNum(10l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120014"));
        line.setOrderNum(11l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120003"));
        line.setOrderNum(12l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120029"));
        line.setOrderNum(13l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 35 кВ - отдача");

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120030"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120031"));
        line.setOrderNum(2l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120018"));
        line.setOrderNum(3l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120033"));
        line.setOrderNum(4l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 6 кВ - отдача");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120021"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120022"));
        line.setOrderNum(2l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120023"));
        line.setOrderNum(3l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120039"));
        line.setOrderNum(4l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120040"));
        line.setOrderNum(5l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120041"));
        line.setOrderNum(6l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120042"));
        line.setOrderNum(7l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120043"));
        line.setOrderNum(8l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120044"));
        line.setOrderNum(9l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 10 кВ - отдача");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120035"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120036"));
        line.setOrderNum(2l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120037"));
        line.setOrderNum(3l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120038"));
        line.setOrderNum(3l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 10 кВ - собственные нужды");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120019"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120020"));
        line.setOrderNum(2l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120027"));
        line.setOrderNum(3l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120028"));
        line.setOrderNum(4l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 0.4 кВ - собственные нужды");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120024"));
        line.setOrderNum(1l);
        lines.add(line);

        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120025"));
        line.setOrderNum(2l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);


        header = new GroupHeader();
        header.setName("Шины 0.4 кВ - хозяйственные нужды");

        lines = new ArrayList<>();
        line = new GroupLine();
        line.setGroupHeader(header);
        line.setMeteringPoint(meteringPointRepo.findByCode("121420300070120026"));
        line.setOrderNum(1l);
        lines.add(line);

        groupHeaderRepo.save(header);
        groupLineRepo.save(lines);
    }
}
