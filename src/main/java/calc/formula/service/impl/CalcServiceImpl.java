package calc.formula.service.impl;

import calc.entity.Formula;
import calc.entity.MeteringPoint;
import calc.entity.PeriodTimeValue;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.FormulaRepo;
import calc.repo.SourceTypeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalcServiceImpl implements CalcService {
    private final ExpressionService expressionService;
    private final FormulaRepo formulaRepo;
    private final SourceTypeRepo sourceTypeRepo;

    @Override
    public PeriodTimeValue calc(String formula, CalcContext context) throws Exception {
        Double result = expressionService
            .parse(formula, context)
            .value();

        PeriodTimeValue pt = new PeriodTimeValue();
        pt.setVal(result);

        return pt;
    }

    @Override
    public List<PeriodTimeValue> calc(CalcContext context) throws Exception {
        Map<String, Expression> expressionMap = new HashMap<>();
        Map<String, Formula>  formulaMap = new HashMap<>();

        for (Formula mpf : formulaRepo.findAllByMeteringPointOrgId(context.getOrgId())) {
            if (mpf.getStartDate() !=null && context.getEndDate().atStartOfDay().isBefore(mpf.getStartDate()))
                continue;

            if (mpf.getEndDate()!=null && context.getStartDate().atStartOfDay().isAfter(mpf.getEndDate()))
                continue;

            //if (mpf.getMeteringPoint().getMeteringPointTypeId()!=2)
            //    continue;

            Expression expr = expressionService.parse(mpf.getText(), context);
            expressionMap.putIfAbsent(mpf.getMeteringPoint().getCode(), expr);
            formulaMap.putIfAbsent(mpf.getMeteringPoint().getCode(), mpf);
        }

        List<String> mps = expressionService.sort(expressionMap);

        context.setPtValues(new ArrayList<>());
        for (String code : mps) {
            Expression expression = expressionMap.get(code);
            Double[] results = expression.values();
            for (int i=0; i<results.length; i++) {
                Formula formula = formulaMap.get(code);
                MeteringPoint meteringPoint = formula.getMeteringPoint();

                PeriodTimeValue pt = new PeriodTimeValue();
                pt.setVal(results[i]);
                pt.setMeteringDate(context.getStartDate().atStartOfDay().plusHours(i));
                pt.setMeteringPointId(meteringPoint.getId());
                pt.setParamId(formula.getParameter().getId());
                pt.setInterval(formula.getInterval());
                pt.setUnitId(formula.getUnit().getId());
                pt.setStatus("OK");
                pt.setSourceType(sourceTypeRepo.findOne(4l));

                context.getPtValues().add(pt);
            }
        }

        return context.getPtValues();
    }
}
