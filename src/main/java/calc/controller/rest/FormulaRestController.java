package calc.controller.rest;

import calc.controller.rest.dto.FormulaDto;
import calc.entity.Formula;
import calc.formula.CalcContext;
import calc.controller.rest.dto.CalcDto;
import calc.controller.rest.dto.ResultDto;
import calc.formula.service.ExpressionService;
import calc.repo.FormulaRepo;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.dozer.DozerBeanMapper;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.Charset;

@RestController
@RequiredArgsConstructor
public class FormulaRestController {
    private final ExpressionService expressionService;
    private final FormulaRepo formulaRepo;
    private final DozerBeanMapper mapper;

    @GetMapping(value = "/rest/formula/{id}", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public FormulaDto getById(@PathVariable Long id) {
        Formula formula = formulaRepo.findOne(id);
        FormulaDto dto = mapper.map(formula, FormulaDto.class);
        dto.setTextBase64(new String(Base64.encodeBase64(formula.getText().getBytes())));
        return dto;
    }

    @GetMapping(value = "/rest/formula/byCode/{code}", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public FormulaDto getByCode(@PathVariable String code) {
        Formula formula = formulaRepo.findByCode(code);
        FormulaDto dto = mapper.map(formula, FormulaDto.class);
        dto.setTextBase64(new String(Base64.encodeBase64(formula.getText().getBytes())));
        return dto;
    }

    @PostMapping(value = "/rest/formula/calc", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public ResultDto calc(@RequestBody CalcDto calcDto) {
        byte[] contentAsBytes = Base64.decodeBase64(calcDto.getContentBase64());
        String formula = new String(contentAsBytes, Charset.forName("UTF-8"));

        CalcContext context = CalcContext.builder()
            .startDate(calcDto.getStartDate().atStartOfDay())
            .endDate(calcDto.getEndDate().atStartOfDay().plusDays(1))
            .build();

        Double result;
        try {
            result = expressionService
                .parse(formula, context)
                .value();
        }
        catch (Exception e) {
            e.printStackTrace();
            result = null;
        }

        return new ResultDto(result);
    }
}
