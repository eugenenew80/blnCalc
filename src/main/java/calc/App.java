package calc;

import calc.entity.rep.*;
import calc.entity.rep.enums.AttrTypeEnum;
import calc.entity.rep.enums.TablePartEnum;
import calc.entity.rep.enums.ValueTypeEnum;
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

    @Autowired
    private ReportRepo reportRepo;

    @Autowired
    private RowTemplateRepo rowTemplateRepo;

    @Autowired
    private SheetRepo sheetRepo;

    @Autowired
    private ColumnRepo sheetColumnRepo;

    @Autowired
    private TableRepo tableRepo;

    @Autowired
    private DivisionRepo divisionRepo;

    @Autowired
    private SectionRepo sectionRepo;

    @Autowired
    private AttrRepo attrRepo;


    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @PostConstruct
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void init() throws Exception {
        Report templateReport = createActTemplate();
        Report report = createActReport(templateReport);
        buildReport(report);
    }

    private void buildReport(Report report) throws Exception {
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

        report = reportRepo.findOne(report.getId());
        Document document = executorService.buildReport(report.getId(), context);
        executorService.save(document, "files/doc.xml");

        Document doc = executorService.transform(document);
        executorService.save(doc, "files/report.xml");
    }

    private Report createActReport(Report templateReport) {
        Report report = reportBuilder.createFromTemplate(templateReport.getCode());

        for (TableSection section : report.getSections()) {
            List<Pair<String, String>> params = null;

            if (section.getCode() != null && section.getCode().equals("1.1")) {
                params = Arrays.asList(
                    Pair.of("121420300070120001", "A-"),
                    Pair.of("121420300070120002", "A-")
                );
            }

            if (section.getCode() != null && section.getCode().equals("1.2")) {
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

            if (section.getCode() != null && section.getCode().equals("1.3")) {
                params = Arrays.asList(
                    Pair.of("121420300070120015", "A-"),
                    Pair.of("121420300070120016", "A-")
                );
            }

            if (section.getCode() != null && section.getCode().equals("2.1")) {
                params = Arrays.asList(
                    Pair.of("121420300070120001", "A+"),
                    Pair.of("121420300070120002", "A+")
                );
            }

            if (section.getCode() != null && section.getCode().equals("2.2")) {
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

            if (section.getCode() != null && section.getCode().equals("2.3")) {
                params = Arrays.asList(
                    Pair.of("121420300070120030", "A+"),
                    Pair.of("121420300070120031", "A+"),
                    Pair.of("121420300070120018", "A+"),
                    Pair.of("121420300070120033", "A+")
                );
            }

            if (section.getCode() != null && section.getCode().equals("2.4")) {
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

            if (section.getCode() != null && section.getCode().equals("2.5")) {
                params = Arrays.asList(
                    Pair.of("121420300070120035", "A+"),
                    Pair.of("121420300070120036", "A+"),
                    Pair.of("121420300070120037", "A+"),
                    Pair.of("121420300070120038", "A+")
                );
            }

            if (section.getCode() != null && section.getCode().equals("3.1")) {
                params = Arrays.asList(
                    Pair.of("121420300070120019", "A-"),
                    Pair.of("121420300070120020", "A-"),
                    Pair.of("121420300070120027", "A-"),
                    Pair.of("121420300070120028", "A-")
                );
            }

            if (section.getCode() != null && section.getCode().equals("3.2")) {
                params = Arrays.asList(
                    Pair.of("121420300070120024", "A-"),
                    Pair.of("121420300070120025", "A-")
                );
            }

            if (section.getCode() != null && section.getCode().equals("3.3")) {
                params = Arrays.asList(
                    Pair.of("121420300070120026", "A-")
                );
            }

            reportBuilder.generateSectionRows(section, params);
        }

        report = reportRepo.findOne(report.getId());
        for (ReportTable table : report.getTables()) {
            for (TableDivision division : table.getDivisions()) {
                if (division.getBelongTo() != TablePartEnum.BODY)
                    continue;

                for (TableSection section : division.getSections()) {
                    if (section.getHasTotal())
                        reportBuilder.generateSectionTotals(section);
                }

                if (division.getHasTotal())
                    reportBuilder.generateDivisionTotals(division);
            }
        }

        return report;
    }

    private Report createActTemplate() {
        RowTemplate rowTemplate = new RowTemplate();
        rowTemplate.setName("Баланс по подстанции - табличная часть");
        rowTemplateRepo.save(rowTemplate);

        TableAttr attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("num");
        attr.setAttrType(AttrTypeEnum.STRING);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setDescription("№ п/п");
        attr.setOrderNum(1l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("serial");
        attr.setAttrType(AttrTypeEnum.STRING);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setDescription("№ счетчиков");
        attr.setOrderNum(2l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("name");
        attr.setAttrType(AttrTypeEnum.STRING);
        attr.setValueType(ValueTypeEnum.FORMULA);
        attr.setDescription("Наименование объектов");
        attr.setFormulaTemplate("<mp code=\"#code#\" attr=\"#attr#\" />");
        attr.setOrderNum(3l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("end");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.FORMULA);
        attr.setDescription("Показ. на конец периода");
        attr.setFormulaTemplate("<at mp=\"#code#\" param=\"#param#\" per=\"end\" />");
        attr.setPrecision(0l);
        attr.setOrderNum(4l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("start");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.FORMULA);
        attr.setDescription("Показ. на начала периода");
        attr.setFormulaTemplate("<at mp=\"#code#\" param=\"#param#\" per=\"start\" />");
        attr.setPrecision(0l);
        attr.setOrderNum(5l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("rate");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setDescription("Коэф-т счетчиков");
        attr.setPrecision(2l);
        attr.setOrderNum(6l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("amount");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.FORMULA);
        attr.setDescription("К-во э/э,  учтенн. Счетчиком, кВт*час");
        attr.setPrecision(0l);
        attr.setOrderNum(7l);
        attr.setFormulaTemplate("" +
                "<subtract>\n" +
                "\t<at mp=\"#code#\" param=\"#param#\" per=\"end\" /> \n" +
                "\t<at mp=\"#code#\" param=\"#param#\" per=\"start\" />\n" +
                "</subtract>");
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("proportion");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setDescription("Доля полученной (отпущенной) электроэнергии");
        attr.setPrecision(4l);
        attr.setOrderNum(8l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("error");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setDescription("Средне-квадратичная  погрешность");
        attr.setPrecision(4l);
        attr.setOrderNum(9l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(rowTemplate);
        attr.setName("under-count");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setDescription("Допустимый небаланс");
        attr.setPrecision(4l);
        attr.setOrderNum(10l);
        attrRepo.save(attr);


        RowTemplate footerTemplate = new RowTemplate();
        footerTemplate.setName("Баланс по подстанции - подвальная часть");
        rowTemplateRepo.save(footerTemplate);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setName("name");
        attr.setAttrType(AttrTypeEnum.STRING);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setDescription("Наименование");
        attr.setOrderNum(1l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(2l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(3l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(4l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(5l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setName("unit");
        attr.setAttrType(AttrTypeEnum.STRING);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(6l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setName("amount");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.FORMULA);
        attr.setPrecision(0l);
        attr.setOrderNum(7l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(8l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(9l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(footerTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(10l);
        attrRepo.save(attr);


        RowTemplate totalTemplate = new RowTemplate();
        totalTemplate.setName("Баланс по подстанции - табличная часть, итоги");
        rowTemplateRepo.save(totalTemplate);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setName("name");
        attr.setAttrType(AttrTypeEnum.STRING);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setDescription("Наименование");
        attr.setOrderNum(1l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(2l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(3l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(4l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(5l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(6l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setName("amount");
        attr.setAttrType(AttrTypeEnum.NUMBER);
        attr.setValueType(ValueTypeEnum.FORMULA);
        attr.setPrecision(0l);
        attr.setOrderNum(7l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(8l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(9l);
        attrRepo.save(attr);

        attr = new TableAttr();
        attr.setRowTemplate(totalTemplate);
        attr.setAttrType(AttrTypeEnum.EMPTY);
        attr.setValueType(ValueTypeEnum.CONST);
        attr.setOrderNum(10l);
        attrRepo.save(attr);


        Report templateReport = new Report();
        templateReport.setName("АКТ");
        templateReport.setIsTemplate(true);
        templateReport.setCode("ACT-SUBST");
        reportRepo.save(templateReport);

        ReportSheet templateSheet = new ReportSheet();
        templateSheet.setReport(templateReport);
        templateSheet.setColumnCount(10l);
        templateSheet.setRowCount(65000l);
        templateSheet.setOrderNum(1l);
        templateSheet.setName("Акт съёма показаний");
        sheetRepo.save(templateSheet);

        SheetColumn templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(1l);
        templateColumn.setWidth(26l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(2l);
        templateColumn.setWidth(72l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(3l);
        templateColumn.setWidth(186l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(4l);
        templateColumn.setWidth(75l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(5l);
        templateColumn.setWidth(72l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(6l);
        templateColumn.setWidth(60l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(7l);
        templateColumn.setWidth(78l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(8l);
        templateColumn.setWidth(72l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(9l);
        templateColumn.setWidth(66l);
        sheetColumnRepo.save(templateColumn);

        templateColumn = new SheetColumn();
        templateColumn.setReport(templateReport);
        templateColumn.setSheet(templateSheet);
        templateColumn.setOrderNum(10l);
        templateColumn.setWidth(60l);
        sheetColumnRepo.save(templateColumn);


        ReportTable templateTable = new ReportTable();
        templateTable.setReport(templateReport);
        templateTable.setSheet(templateSheet);
        templateTable.setOrderNum(1l);
        templateTable.setName("Показания счётчиков");
        templateTable.setBodyRowTemplate(rowTemplate);
        templateTable.setBodyTotalTemplate(totalTemplate);
        templateTable.setFooterRowTemplate(footerTemplate);
        templateTable.setHasFooter(true);
        templateTable.setHasHeader(true);
        tableRepo.save(templateTable);


        TableDivision templateDivision = new TableDivision();
        templateDivision.setCode("1");
        templateDivision.setName("1. Прием от энергосистемы (импорт)");
        templateDivision.setBelongTo(TablePartEnum.BODY);
        templateDivision.setHasTitle(true);
        templateDivision.setHasTotal(true);
        templateDivision.setOrderNum(1l);
        templateDivision.setReport(templateReport);
        templateDivision.setSheet(templateSheet);
        templateDivision.setTable(templateTable);
        divisionRepo.save(templateDivision);

        TableSection templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(1l);
        templateSection.setCode("1.1");
        templateSection.setName("1.1 Шины 500 кВ");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(1l);
        templateSection.setCode("1.2");
        templateSection.setName("1.2 Шины 220 кВ");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(1l);
        templateSection.setCode("1.3");
        templateSection.setName("1.3 Шины 220 кВ Ввода");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);


        templateDivision = new TableDivision();
        templateDivision.setCode("2");
        templateDivision.setName("2. Отдача в энергосистему (экспорт)");
        templateDivision.setBelongTo(TablePartEnum.BODY);
        templateDivision.setHasTitle(true);
        templateDivision.setHasTotal(true);
        templateDivision.setOrderNum(2l);
        templateDivision.setReport(templateReport);
        templateDivision.setSheet(templateSheet);
        templateDivision.setTable(templateTable);
        divisionRepo.save(templateDivision);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(1l);
        templateSection.setCode("2.1");
        templateSection.setName("2.1 Шины 500 кВ");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(2l);
        templateSection.setCode("2.2");
        templateSection.setName("2.2 Шины 220 кВ");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(3l);
        templateSection.setCode("2.3");
        templateSection.setName("2.3 Шины 35 кВ");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(4l);
        templateSection.setCode("2.4");
        templateSection.setName("2.4 Шины 6 кВ");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(5l);
        templateSection.setCode("2.5");
        templateSection.setName("2.5 Шины 10 кВ УШР");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);


        templateDivision = new TableDivision();
        templateDivision.setCode("3");
        templateDivision.setName("3. Расход на хозяйственные и собственные нужды");
        templateDivision.setBelongTo(TablePartEnum.BODY);
        templateDivision.setHasTitle(true);
        templateDivision.setHasTotal(true);
        templateDivision.setOrderNum(3l);
        templateDivision.setReport(templateReport);
        templateDivision.setSheet(templateSheet);
        templateDivision.setTable(templateTable);
        divisionRepo.save(templateDivision);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(1l);
        templateSection.setCode("3.1");
        templateSection.setName("3.1 Шины 500 кВ");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(2l);
        templateSection.setCode("3.2");
        templateSection.setName("3.2 Шины 0.4 кВ собственные нужды");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);

        templateSection = new TableSection();
        templateSection.setReport(templateReport);
        templateSection.setSheet(templateSheet);
        templateSection.setTable(templateTable);
        templateSection.setDivision(templateDivision);
        templateSection.setOrderNum(3l);
        templateSection.setCode("3.3");
        templateSection.setName("3.3 Шины 0.4 кВ хозяйственные нужды");
        templateSection.setHasTotal(true);
        templateSection.setHasTitle(true);
        sectionRepo.save(templateSection);


        templateDivision = new TableDivision();
        templateDivision.setCode("4");
        templateDivision.setName("4. Потери в э/э понижающих трансформаторах");
        templateDivision.setBelongTo(TablePartEnum.FOOTER);
        templateDivision.setHasTitle(true);
        templateDivision.setHasTotal(true);
        templateDivision.setOrderNum(3l);
        templateDivision.setReport(templateReport);
        templateDivision.setSheet(templateSheet);
        templateDivision.setTable(templateTable);
        divisionRepo.save(templateDivision);

        templateDivision = new TableDivision();
        templateDivision.setCode("5");
        templateDivision.setName("5. Расчет допустимого небаланса");
        templateDivision.setBelongTo(TablePartEnum.FOOTER);
        templateDivision.setHasTitle(true);
        templateDivision.setHasTotal(true);
        templateDivision.setOrderNum(3l);
        templateDivision.setReport(templateReport);
        templateDivision.setSheet(templateSheet);
        templateDivision.setTable(templateTable);
        divisionRepo.save(templateDivision);

        templateDivision = new TableDivision();
        templateDivision.setCode("6");
        templateDivision.setName("6. Баланс э/э на подстанции");
        templateDivision.setBelongTo(TablePartEnum.FOOTER);
        templateDivision.setHasTitle(true);
        templateDivision.setHasTotal(true);
        templateDivision.setOrderNum(3l);
        templateDivision.setReport(templateReport);
        templateDivision.setSheet(templateSheet);
        templateDivision.setTable(templateTable);
        divisionRepo.save(templateDivision);
        return templateReport;
    }


    @Autowired
    private ReportExecutorService executorService;

    @Autowired
    private ReportBuilder reportBuilder;
}
