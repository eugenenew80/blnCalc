package calc.controller.rest;

import calc.controller.rest.dto.CalcDto;
import calc.controller.rest.dto.ResultDto;
import calc.formula.CalcContext;
import calc.formula.service.ExpressionService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@RestController
@RequiredArgsConstructor
public class CalcController {
    private final ExpressionService expressionService;

    @PostConstruct
    private void init() {
    }

    @PostMapping(value = "/rest/calc/text", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public ResultDto calc(@RequestBody CalcDto calcDto) throws Exception {
        byte[] contentAsBytes = Base64.decodeBase64(calcDto.getContentBase64());
        String formula = new String(contentAsBytes, Charset.forName("UTF-8"));

        CalcContext context = CalcContext.builder()
            .startDate(calcDto.getStartDate().atStartOfDay())
            .endDate(calcDto.getEndDate().atStartOfDay().plusDays(1))
            .build();

        Double result = expressionService
            .parse(formula, context)
            .value();

        return new ResultDto(result);
    }
}
