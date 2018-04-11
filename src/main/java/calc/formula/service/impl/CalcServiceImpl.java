package calc.formula.service.impl;

import calc.controller.rest.dto.ResultDto;
import calc.entity.MeteringPointFormula;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.MeteringPointFormulaRepo;
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

    @Override
    public ResultDto getResult(String formula, CalcContext context) throws Exception {
        Double result = expressionService
            .parse(formula, context)
            .value();

        return new ResultDto(result);

    }

    @Override
    public List<ResultDto> getResult(CalcContext context) throws Exception {
        Map<String, Expression> expressionMap = new HashMap<>();
        for (MeteringPointFormula mpf : meteringPointFormulaRepo.findAllByMeteringPointOrgId(context.getOrgId())) {
            if (mpf.getStartDate() !=null && context.getEndDate().isBefore(mpf.getStartDate()))
                continue;

            if (mpf.getEndDate()!=null && context.getStartDate().isAfter(mpf.getEndDate()))
                continue;

            //if (mpf.getMeteringPoint().getMeteringPointTypeId()!=2)
            //    continue;

            Expression expr = expressionService.parse(mpf.getFormula().getText(), context);
            expressionMap.putIfAbsent(mpf.getMeteringPoint().getCode(), expr);
        }

        List<String> mps = expressionService.sort(expressionMap);
        List<ResultDto> resultList = new ArrayList<>();
        for (String code : mps) {
            Double value = expressionMap.get(code).value();
            resultList.add(new ResultDto(value));
        }

        return resultList;
    }
}
