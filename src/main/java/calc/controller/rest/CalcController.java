package calc.controller.rest;

import calc.controller.rest.dto.ContextDto;
import calc.controller.rest.dto.ResultDto;
import calc.entity.MeteringPointFormula;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.ExpressionService;
import calc.repo.MeteringPointFormulaRepo;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.*;


@RestController
@RequiredArgsConstructor
public class CalcController {
    private final ExpressionService expressionService;
    private final MeteringPointFormulaRepo meteringPointFormulaRepo;

    @PostConstruct
    private void init() { }

    @PostMapping(value = "/rest/calc/text", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public ResultDto calc(@RequestBody ContextDto contextDto) throws Exception {
        byte[] contentAsBytes = Base64.decodeBase64(contextDto.getContentBase64());
        String formula = new String(contentAsBytes, Charset.forName("UTF-8"));

        CalcContext context = CalcContext.builder()
            .startDate(contextDto.getStartDate().atStartOfDay())
            .endDate(contextDto.getEndDate().atStartOfDay().plusDays(1))
            .orgId(contextDto.getOrgId())
            .build();

        Double result = expressionService
            .parse(formula, context)
            .value();

        return new ResultDto(result);
    }


    @PostMapping(value = "/rest/calc/all", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public void calcAll(@RequestBody ContextDto contextDto) throws Exception {
        CalcContext context = CalcContext.builder()
            .startDate(contextDto.getStartDate().atStartOfDay())
            .endDate(contextDto.getEndDate().atStartOfDay().plusDays(1))
            .orgId(3l)
            .build();

        Map<String, Expression> expressionMap = new HashMap<>();
        for (MeteringPointFormula mpf : meteringPointFormulaRepo.findAllByMeteringPointOrgId(context.getOrgId())) {
            if (mpf.getStartDate() !=null && context.getEndDate().isBefore(mpf.getStartDate()))
                continue;

            if (mpf.getEndDate()!=null && context.getStartDate().isAfter(mpf.getEndDate()))
                continue;

            if (mpf.getMeteringPoint().getMeteringPointTypeId()!=2)
                continue;

            Expression expr = expressionService.parse(mpf.getFormula().getText(), context);
            expressionMap.putIfAbsent(mpf.getMeteringPoint().getCode(), expr);
        }

        List<String> mps = expressionService.sort(expressionMap);
        for (String code : mps) {
            Double value = expressionMap.get(code).value();
            System.out.println(code + ": " + value);
        }
    }
}
