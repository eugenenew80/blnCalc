package calc.controller.rest;

import calc.controller.rest.dto.ContextDto;
import calc.entity.PeriodTimeValue;
import calc.formula.CalcContext;
import calc.formula.service.CalcService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CalcController {
    private final CalcService calcService;

    @PostConstruct
    private void init() { }

    @PostMapping(value = "/rest/calc/text", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public PeriodTimeValue calc(@RequestBody ContextDto contextDto) throws Exception {
        byte[] contentAsBytes = Base64.decodeBase64(contextDto.getContentBase64());
        String formula = new String(contentAsBytes, Charset.forName("UTF-8"));

        CalcContext context = CalcContext.builder()
            .startDate(contextDto.getStartDate())
            .endDate(contextDto.getEndDate())
            .orgId(contextDto.getOrgId())
            .build();

        return calcService.calc(formula, context);
    }

    @PostMapping(value = "/rest/calc/all", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public List<PeriodTimeValue> calcAll(@RequestBody ContextDto contextDto) throws Exception {
        CalcContext context = CalcContext.builder()
            .startDate(contextDto.getStartDate())
            .endDate(contextDto.getEndDate())
            .orgId(3l)
            .build();

        List<PeriodTimeValue> list = calcService.calc(context);
        list.stream().forEach( t -> System.out.println(t));

        return list;
    }
}
