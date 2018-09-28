package calc.rest;

import calc.entity.calc.Formula;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.repo.calc.FormulaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FormulaRestController {
    private final CalcService calcService;
    private final FormulaRepo formulaRepo;

    @PostMapping(value = "/rest/formuala/calc", produces = "application/json")
    public ResultDto create(@RequestBody FormulaDto formulaDto) {
        Formula formula = formulaRepo.findOne(formulaDto.getFormulaId());
        LocalDate startDate = LocalDate.of(2018, Month.AUGUST, 2);
        LocalDate endDate   = LocalDate.of(2018, Month.AUGUST, 2);

        CalcContext context = CalcContext.builder()
            .docCode("TEST")
            .docId(formula.getId())
            .headerId(formula.getId())
            .periodType(PeriodTypeEnum.H)
            .startDate(startDate)
            .endDate(endDate)
            .orgId(1l)
            .trace(new HashMap<>())
            .values(new HashMap<>())
            .build();

        ResultDto resultDto = new ResultDto();
        resultDto.setFormulaId(formulaDto.getFormulaId());
        try {
            List<CalcResult> results = calcService.calcFormulas(Arrays.asList(formula), context);
            if (context.getException()!=null)
                throw context.getException();

           if (!results.isEmpty()) {
               CalcResult result = results.get(0);
               if (formula.getParamType() == ParamTypeEnum.AT)
                   resultDto.setVal(result.getDoubleValue());
               else {
                   Double sum =0d;
                   for (int i=0; i<result.getDoubleValues().length; i++)
                       if (result.getDoubleValues()[i] != null) sum += result.getDoubleValues()[i];
                  resultDto.setVal(sum);
               }

               resultDto.setStatus("S");
               resultDto.setMsg("Формула синтаксички верная");
            }
        }
        catch (Exception e) {
            resultDto.setStatus("E");
            resultDto.setMsg(e.getMessage());
            e.printStackTrace();
        }

        return resultDto;
    }
}
