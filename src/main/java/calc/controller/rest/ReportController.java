package calc.controller.rest;

import calc.controller.rest.dto.ContextDto;
import calc.formula.CalcContext;
import calc.rep.TemplateReportBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class ReportController {
    private final TemplateReportBuilder reportService;

    @PostMapping(value = "/rest/report/build/{id}", consumes = "application/json;charset=utf-8")
    public void calc(@PathVariable Long id, @RequestBody ContextDto contextDto) throws Exception {
        CalcContext context = CalcContext.builder()
            .startDate(contextDto.getStartDate())
            .endDate(contextDto.getEndDate())
            .orgId(contextDto.getOrgId())
            .orgName("Южные МЭС")
            .reportName("АКТ")
            .energyObjectType("SUBST")
            .energyObjectId(11l)
            .energyObjectName("ПС Шымкент 500")
            .trace(new HashMap<>())
            .values(new ArrayList<>())
            .build();

        Document document = reportService.buildReport(id, context);
        Document doc = reportService.transform(document);
        reportService.save(doc, "files/report.xml");
    }
}
