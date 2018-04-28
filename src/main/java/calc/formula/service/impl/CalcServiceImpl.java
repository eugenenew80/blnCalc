package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.formula.CalcResult;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.calc.FormulaRepo;
import calc.repo.calc.SourceTypeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalcServiceImpl implements CalcService {
    private final ExpressionService expressionService;
    private final FormulaRepo formulaRepo;
    private final SourceTypeRepo sourceTypeRepo;

    @Override
    public CalcResult calc(String text, CalcContext context) throws Exception {
        Formula formula = new Formula();
        formula.setText(text);

        Double value = expressionService
            .parse(formula, context)
            .value();

        CalcResult result = new CalcResult();
        result.setVal(value);

        return result;
    }

    @Override
    public List<CalcResult> calc(CalcContext context) throws Exception {
        Map<String, Expression> expressionMap = new HashMap<>();
        Map<String, Formula>  formulaMap = new HashMap<>();

        for (Formula formula : formulaRepo.findAllByMeteringPointOrgId(context.getOrgId())) {
            if (formula.getStartDate() !=null && context.getEndDate().atStartOfDay().isBefore(formula.getStartDate()))
                continue;

            if (formula.getEndDate()!=null && context.getStartDate().atStartOfDay().isAfter(formula.getEndDate()))
                continue;

            if (formula.getMeteringPoint().getMeteringPointTypeId()!=2)
                continue;

            Expression expr = expressionService.parse(formula, context);

            expressionMap.putIfAbsent(formula.getMeteringPoint().getCode(), expr);
            formulaMap.putIfAbsent(formula.getMeteringPoint().getCode(), formula);
        }

        List<String> mpCodes = expressionService.sort(expressionMap);
        context.setValues(new ArrayList<>());
        context.setTrace(new HashMap<>());
        for (String code : mpCodes) {
            Formula formula = formulaMap.get(code);
            MeteringPoint meteringPoint = formula.getMeteringPoint();

            for (MeteringPointParameter fp : meteringPoint.getParameters()) {
                Parameter parameter = fp.getParameter();
                Unit unit = parameter.getUnit();
                Expression expression = expressionService.parse(formula, parameter.getCode(), context);

                Double[] results;
                LocalDateTime meteringDate;
                Long interval;
                if (parameter.getParamType().equals("PT")) {
                    results = expression.values();
                    meteringDate = context.getStartDate().atStartOfDay();
                    interval = 3600l;
                } else {
                    results = new Double[]{expression.value()};
                    meteringDate = context.getEndDate().atStartOfDay().plusDays(1);
                    interval = null;
                }

                for (int i = 0; i < results.length; i++) {
                    CalcResult result = new CalcResult();
                    result.setMeteringDate(meteringDate.plusHours(i));
                    result.setVal(results[i]);
                    result.setInterval(interval);
                    result.setMeteringPointId(meteringPoint.getId());
                    result.setParamId(parameter.getId());
                    result.setUnitId(unit.getId());
                    result.setParamType(parameter.getParamType());
                    context.getValues().add(result);
                }
            }
        }

        return context.getValues();
    }
}
