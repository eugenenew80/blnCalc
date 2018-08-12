package calc.formula.doc.impl;

import calc.entity.calc.*;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.doc.DocService;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.calc.BalanceSubstHeaderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class UnBalanceSubstServiceImpl implements DocService {
    private final CalcService calcService;
    private final ExpressionService expressionService;
    private final BalanceSubstHeaderRepo balanceSubstHeaderRepo;

    public Map<String, List<CalcResult>> calc(TaskParam taskParam) throws Exception {
        Map<String, List<CalcResult>> allResults = new HashMap<>();

        CalcContext context = createContext(taskParam);
        Map<String, Map<String, DoubleExpression>> expressions = createFormulas(context);

        for (String section : expressions.keySet()) {
            List<CalcResult> results = new ArrayList<>();
            List<String> mpCodes = expressionService.sort(expressions.get(section));

            for (String mpCode : mpCodes) {
                DoubleExpression expression = expressions.get(section).get(mpCode);
                try {
                    CalcResult result = calcService.calc(expression, context);
                    results.add(result);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            allResults.putIfAbsent(section, results);
        }

        return allResults;
    }

    private CalcContext createContext(TaskParam taskParam) {
        return CalcContext.builder()
            .startDate(taskParam.getStartDate())
            .endDate(taskParam.getEndDate())
            .orgId(taskParam.getOrg().getId())
            .energyObjectType("SUBSTATION")
            .energyObjectId(taskParam.getSubstation().getId())
            .docCode(taskParam.getTask().getDocCode())
            .docId(taskParam.getTask().getId())
            .trace(new HashMap<>())
            .values(new ArrayList<>())
            .build();
    }


    private Map<String, Map<String, DoubleExpression>> createFormulas(CalcContext context) throws Exception {
        Map<String, Map<String, DoubleExpression>> expressions = new HashMap<>();

        expressions.putIfAbsent("1",  new HashMap<>());
        expressions.putIfAbsent("2",  new HashMap<>());
        expressions.putIfAbsent("3",  new HashMap<>());
        expressions.putIfAbsent("4",  new HashMap<>());

        BalanceSubstHeader header = balanceSubstHeaderRepo.findOne(context.getDocId());
        for (BalanceSubstUbLine line : header.getUbLines()) {
            for (Formula formula : createFormula(line)) {
                DoubleExpression expr = expressionService.parse(formula, context);

                if (line.getIsSection1())
                    expressions.get("1").putIfAbsent(formula.getCode() + "V", expr);

                if (line.getIsSection2())
                    expressions.get("2").putIfAbsent(formula.getCode() + "V", expr);
            }
        }

        return expressions;
    }

    private List<Formula> createFormula(BalanceSubstUbLine line) {
        String mpCode = line.getMeteringPoint().getCode();
        List<Formula> formulas= new ArrayList<>();
        for (String param : getParams(line)) {
            Formula formula = new Formula();
            formula.setCode(mpCode);
            formula.setText("<pt mp=\"" + mpCode + "\" param=\"" + param + "\" />");
            formula.setMeteringPoint(line.getMeteringPoint());
            formula.setParam(param);
            formulas.add(formula);
        }
        return formulas;
    }

    private List<String> getParams(BalanceSubstUbLine line) {
        String param = "";
        if (line.getIsSection1())
            param = "A+";

        if (line.getIsSection2())
            param = "A-";

        if (line.getIsSection2())
            param = "A-";

        return Arrays.asList(inverse(param, line.getIsInverse()));
    }

    private String inverse(String param, Boolean isInverse) {
        if (!isInverse) return param;
        if (param.equals("A+")) return "A-";
        if (param.equals("A-")) return "A+";
        if (param.equals("R+")) return "R-";
        if (param.equals("R-")) return "R+";
        return param;
    }
}
