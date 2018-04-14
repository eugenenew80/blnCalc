package calc.formula.service.impl;

import calc.controller.rest.dto.ResultDto;
import calc.entity.Formula;
import calc.entity.MeteringPoint;
import calc.entity.PeriodTimeValue;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.FormulaRepo;
import calc.repo.PeriodTimeValueRepo;
import calc.repo.SourceTypeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CalcServiceImpl implements CalcService {
    private final ExpressionService expressionService;
    private final FormulaRepo formulaRepo;
    private  final PeriodTimeValueRepo periodTimeValueRepo;
    private  final SourceTypeRepo sourceTypeRepo;

    @Override
    public ResultDto getResult(String formula, CalcContext context) throws Exception {
        Double result = expressionService
            .parse(formula, context)
            .value();

        return new ResultDto(null, result);

    }

    @Override
    public void getResult(CalcContext context) throws Exception {
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
        List<PeriodTimeValue> ptValues = new ArrayList<>();

        context.setPtValues(new ArrayList<>());
        for (String code : mps) {
            Expression expression = expressionMap.get(code);
            System.out.println(expression.getClass());

            Double result = expression.value();
            Double[] results = expression.values();
            System.out.println(Arrays.asList(results));

            Formula formula = formulaMap.get(code);
            MeteringPoint meteringPoint = formula.getMeteringPoint();

            PeriodTimeValue pt = periodTimeValueRepo.findAllByMeteringPointIdAndParamIdAndMeteringDateBetween(meteringPoint.getId(), formula.getParameter().getId(), context.getEndDate().atStartOfDay().plusDays(1), context.getEndDate().atStartOfDay().plusDays(1))
                .stream()
                .filter(t -> t.getSourceType().getId() == 4l)
                .findFirst()
                .orElse(null);

            if (pt==null) {
                pt = new PeriodTimeValue();
                pt.setMeteringDate(context.getEndDate().atStartOfDay().plusDays(1));
                pt.setMeteringPointId(meteringPoint.getId());
                pt.setParamId(formula.getParameter().getId());
                pt.setSourceType(sourceTypeRepo.findOne(4l));
            }

            pt.setInterval(formula.getInterval());
            pt.setUnitId(formula.getUnit().getId());
            pt.setStatus("OK");
            pt.setVal(result);

            context.getPtValues().add(pt);
            ptValues.add(pt);
        }

        periodTimeValueRepo.save(ptValues);
        ptValues.stream().forEach( t -> System.out.println(t));
    }
}
