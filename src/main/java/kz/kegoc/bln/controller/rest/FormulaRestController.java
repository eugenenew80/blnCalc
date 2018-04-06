package kz.kegoc.bln.controller.rest;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.calc.service.FormulaCalculator;
import kz.kegoc.bln.controller.rest.dto.Base64Dto;
import kz.kegoc.bln.controller.rest.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class FormulaRestController {

    @Autowired
    private final FormulaCalculator formula;

    @PostMapping(value = "/rest/formula/calc", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public ResultDto calc(@RequestBody Base64Dto base64Dto) {
        byte[] contentAsBytes = Base64.decodeBase64(base64Dto.getContentBase64());
        String content = new String(contentAsBytes, Charset.forName("UTF-8"));

        CalcContext context = CalcContext.builder()
            .startDate(base64Dto.getStartDate().atStartOfDay())
            .endDate(base64Dto.getEndDate().atStartOfDay().plusDays(1))
            .build();

        Double result;
        try {
            result = formula.calc(content, context);
        }
        catch (Exception e) {
            e.printStackTrace();
            result = null;
        }

        return new ResultDto(result);
    }
}
