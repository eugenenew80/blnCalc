package calc.formula.service.impl;

import calc.controller.rest.dto.Result;
import calc.entity.*;
import calc.formula.CalcContext;
import calc.formula.CalcInfo;
import calc.formula.expression.Expression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.FormulaRepo;
import calc.repo.SourceTypeRepo;
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
    public Result calc(String text, CalcContext context) throws Exception {
        Formula formula = new Formula();
        formula.setText(text);

        Double value = expressionService
            .parse(formula, context)
            .value();

        Result result = new Result();
        result.setVal(value);

        return result;
    }

    @Override
    public List<Result> calc(CalcContext context) throws Exception {
        Map<String, Expression> expressionMap = new HashMap<>();
        Map<String, Formula>  formulaMap = new HashMap<>();

        for (Formula mpf : formulaRepo.findAllByMeteringPointOrgId(context.getOrgId())) {
            if (mpf.getStartDate() !=null && context.getEndDate().atStartOfDay().isBefore(mpf.getStartDate()))
                continue;

            if (mpf.getEndDate()!=null && context.getStartDate().atStartOfDay().isAfter(mpf.getEndDate()))
                continue;

            if (mpf.getMeteringPoint().getMeteringPointTypeId()!=2)
                continue;

            Expression expr = expressionService.parse(mpf, context);

            expressionMap.putIfAbsent(mpf.getMeteringPoint().getCode(), expr);
            formulaMap.putIfAbsent(mpf.getMeteringPoint().getCode(), mpf);
        }

        List<String> mps = expressionService.sort(expressionMap);
        context.setValues(new ArrayList<>());
        context.setTrace(new HashMap<>());
        for (String code : mps) {
            Expression expression = expressionMap.get(code);
            Formula formula = formulaMap.get(code);
            MeteringPoint meteringPoint = formula.getMeteringPoint();
            Parameter parameter = formula.getParameters().get(0).getParameter();
            Unit unit = parameter.getUnit();

            Double[] results;
            LocalDateTime meteringDate;
            Long interval;
            if (parameter.getParamType().equals("PT")) {
                results = expression.values();
                meteringDate = context.getStartDate().atStartOfDay();
                interval = 3600l;
            }
            else {
                results = new Double[]{expression.value()};
                meteringDate = context.getEndDate().atStartOfDay().plusDays(1);
                interval = null;
            }

            for (int i=0; i<results.length; i++) {
                Result result = new Result();
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

        return context.getValues();
    }
}
