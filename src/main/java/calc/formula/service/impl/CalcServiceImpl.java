package calc.formula.service.impl;

import calc.controller.rest.dto.ResultDto;
import calc.entity.MeteringPointFormula;
import calc.entity.Value;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.MeteringPointFormulaRepo;
import calc.repo.ValueRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalcServiceImpl implements CalcService {
    private final ExpressionService expressionService;
    private final MeteringPointFormulaRepo meteringPointFormulaRepo;
    private final ValueRepo valueRepo;

    @Override
    public ResultDto getResult(String formula, CalcContext context) throws Exception {
        Double result = expressionService
            .parse(formula, context)
            .value();

        return new ResultDto(null, result);

    }

    @Override
    public List<Value> getResult(CalcContext context) throws Exception {
        Map<String, Expression> expressionMap = new HashMap<>();
        Map<String, MeteringPointFormula>  formulaMap = new HashMap<>();

        for (MeteringPointFormula mpf : meteringPointFormulaRepo.findAllByMeteringPointOrgId(context.getOrgId())) {
            if (mpf.getStartDate() !=null && context.getEndDate().atStartOfDay().isBefore(mpf.getStartDate()))
                continue;

            if (mpf.getEndDate()!=null && context.getStartDate().atStartOfDay().isAfter(mpf.getEndDate()))
                continue;

            if (mpf.getMeteringPoint().getMeteringPointTypeId()!=2)
                continue;

            Expression expr = expressionService.parse(mpf.getFormula().getText(), context);
            expressionMap.putIfAbsent(mpf.getMeteringPoint().getCode(), expr);
            formulaMap.putIfAbsent(mpf.getMeteringPoint().getCode(), mpf);
        }

        List<String> mps = expressionService.sort(expressionMap);
        List<Value> values = new ArrayList<>();
        for (String code : mps) {
            Double result = expressionMap.get(code).value();

            Long meteringPointId = formulaMap.get(code).getMeteringPoint().getId();
            Long formulaId = formulaMap.get(code).getFormula().getId();

            Value value = valueRepo.findAllByMeteringPointIdAndStartDateAndEndDate(meteringPointId, context.getStartDate(), context.getEndDate());
            if (value==null) {
                value = new Value();
                value.setStartDate(context.getStartDate());
                value.setEndDate(context.getEndDate());
                value.setMeteringPointId(meteringPointId);
            }

            value.setFormulaId(formulaId);
            value.setVal(result);

            valueRepo.save(value);
            values.add(value);
        }

        return values;
    }
}
