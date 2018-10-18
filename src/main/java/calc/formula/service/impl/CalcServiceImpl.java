package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.FormulaTypeEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.formula.CalcResult;
import calc.formula.CalcContext;
import calc.formula.exception.CycleDetectionException;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.impl.*;
import calc.formula.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import javax.script.ScriptEngine;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class CalcServiceImpl implements CalcService {
    private static final Logger logger = LoggerFactory.getLogger(CalcService.class);
    private final ExpressionService expressionService;
    private final PeriodTimeValueService periodTimeValueService;
    private final AtTimeValueService atTimeValueService;
    private final BalanceSubstResultMrService mrService;
    private final TransformerValueService transformerValueService;
    private final AspResultService aspService;
    private final OperatorFactory operatorFactory;
    private final ScriptEngine engine;

    @Override
    public CalcResult calcMeteringPoint(MeteringPoint point, String param, CalcContext context) throws Exception {
        logger.trace("---------------------------------------------");
        logger.trace("point: " + point.getCode());
        logger.trace("param: " + param);
        logger.trace("periodType: " + context.getPeriodType());

        Formula formula = point.getFormulas()
            .stream()
            .filter(t -> t.getParam().getCode().equals(param))
            .findFirst()
            .orElse(null);

        if (formula == null) {
            logger.trace("Formula not found");
            return null;
        }

        logger.trace("formulaId: " + formula.getId());

        Set<String> set = new HashSet<>();
        Set<MeteringPoint> childPoints =  new HashSet<>(getChildPoints(point, set).values());

        logger.trace("child points:");
        childPoints.forEach(t -> logger.trace("point: " + t.getCode()));

        List<Formula> formulas = childPoints.stream()
            .flatMap(p -> p.getFormulas().stream())
            .collect(Collectors.toList());
        formulas.add(formula);

        logger.trace("all formulas:");
        formulas.forEach(t -> logger.trace("formulaId: " + t.getId() + ", src: " + t.getText()));

        List<CalcResult> results = calcFormulas(formulas, context);

        logger.trace("results:");
        results.forEach(t -> {
            logger.trace("formulaId: " + t.getFormula().getId());
            logger.trace("src: " + t.getFormula().getText());
            logger.trace("point: " + t.getMeteringPoint().getCode());
            logger.trace("param: " + t.getParam().getCode());
            logger.trace("paramType: " + t.getParamType());
            logger.trace("meteringDate: " + t.getMeteringDate());
            logger.trace("periodType: " + t.getPeriodType());

            if (t.getPeriodType() != PeriodTypeEnum.H)
                logger.trace("value: " + t.getDoubleValue());

            if (t.getPeriodType() == PeriodTypeEnum.H)
                logger.trace("values: " + Arrays.deepToString(t.getDoubleValues()));
        });
        logger.trace("---------------------------------------------");

        return results.stream()
            .filter(t -> t.getMeteringPoint().equals(point))
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<CalcResult> calcFormulas(List<Formula> formulas, CalcContext context) throws Exception {
        Map<String, Pair<Formula, DoubleExpression>> expressions = new HashMap<>();
        for (Formula formula : formulas) {
            DoubleExpression expression = buildExpression(formula, context);
            expressions.putIfAbsent(formula.getMeteringPoint().getCode(), Pair.of(formula, expression));
        }

        List<CalcResult> results = new ArrayList<>();
        try {
            List<String> sortedPointCodes = sort(expressions);
            for (String pointCode : sortedPointCodes) {
                Formula formula = expressions.get(pointCode).getFirst();
                DoubleExpression expression = expressions.get(pointCode).getSecond();

                CalcResult result = new CalcResult();
                result.setFormula(formula);
                result.setMeteringDate(context.getEndDate().atStartOfDay().plusDays(1));
                result.setMeteringPoint(formula.getMeteringPoint());
                result.setParam(formula.getParam());
                result.setUnit(formula.getParam().getUnit());
                result.setParamType(formula.getParamType().name());

                if (context.getPeriodType() != PeriodTypeEnum.H) {
                    result.setDoubleValue(expression.doubleValue());
                    result.setPeriodType(context.getPeriodType());
                }

                if (context.getPeriodType() == PeriodTypeEnum.H) {
                    result.setDoubleValues(expression.doubleValues());
                    result.setPeriodType(context.getPeriodType());
                }

                results.add(result);
                cacheResult(context, result);
            }
        }
        catch (CycleDetectionException e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private List<String> sort(Map<String, Pair<Formula, DoubleExpression>> expressions) throws CycleDetectionException {
        Map<String, Set<String>> codesMap = new HashMap<>();
        for (String pointCode : expressions.keySet())
            codesMap.putIfAbsent(pointCode, expressions.get(pointCode).getSecond().pointCodes());

        logger.trace("before sorting: " + codesMap.keySet());
        List<String> pointCodes = expressionService.sort(codesMap);
        logger.trace("after sorting: " + pointCodes);
        return pointCodes;
    }

    private Map<String, MeteringPoint> getChildPoints(MeteringPoint rootPoint, Set<String> set) {
        Map<String, MeteringPoint> points = new ConcurrentHashMap<>();
        rootPoint.getFormulas().stream()
            .flatMap(f -> f.getVars().stream())
            .flatMap(v -> v.getDetails().stream())
            .map(d -> d.getMeteringPoint())
            .forEach(t -> points.putIfAbsent(t.getCode(), t));

        for (String key : points.keySet()) {
            MeteringPoint point = points.get(key);
            set.add(rootPoint.getCode() + "#" + point.getCode());
            if (set.contains(point.getCode() + "#" + rootPoint.getCode()))
                continue;

            Map<String, MeteringPoint> childPoints = getChildPoints(point, set);
            for (String childKey : childPoints.keySet())
                points.putIfAbsent(childKey, childPoints.get(childKey));
        }

        return points;
    }

    private void cacheResult(CalcContext context, CalcResult cacheResult) {
        String code = cacheResult.getMeteringPoint().getCode();
        if (context.getPeriodType() != PeriodTypeEnum.H) {
            List<CalcResult> values = context.getValues().getOrDefault(code, new ArrayList<>());
            values.add(cacheResult);
            context.getValues().put(code, values);
        }

        if (context.getPeriodType() == PeriodTypeEnum.H) {
            List<CalcResult> values = context.getValues().getOrDefault(code, new ArrayList<>());
            LocalDateTime meteringDate = context.getStartDate().atStartOfDay();
            for (int i = 0; i < cacheResult.getDoubleValues().length; i++) {
                CalcResult result = new CalcResult();
                result.setMeteringDate(meteringDate.plusHours(i));
                result.setDoubleValue(cacheResult.getDoubleValues()[i]);
                result.setPeriodType(cacheResult.getPeriodType());
                result.setMeteringPoint(cacheResult.getMeteringPoint());
                result.setParam(cacheResult.getParam());
                result.setUnit(cacheResult.getUnit());
                result.setParamType(cacheResult.getParamType());
                values.add(result);
                context.getValues().put(code, values);
            }
        }
    }

    private DoubleExpression buildExpression(Formula formula, CalcContext context) {
        if (formula.getFormulaType() != FormulaTypeEnum.DIALOG || formula.getText() == null)
            return DoubleValueExpression.builder().build();

        Map<String, DoubleExpression> attrs = new HashMap<>();
        for (FormulaVar var : formula.getVars())
            attrs.putIfAbsent(var.getVarName(), mapVar(var, context));

        return JsExpression.builder()
            .src(formula.getText())
            .attributes(attrs)
            .engine(engine)
            .context(context)
            .build();
    }

    private DoubleExpression mapVar(FormulaVar var, CalcContext context) {
        List<DoubleExpression> expressions = var.getDetails()
            .stream()
            .map(t -> mapDetail(t, context))
            .filter(t -> t != null)
            .collect(toList());

        if (expressions.size()==0) return DoubleValueExpression.builder().build();
        if (expressions.size()==1) return expressions.get(0);

        return BinaryExpression.builder()
            .operator(operatorFactory.binary("add"))
            .expressions(expressions)
            .build();
    }

    private DoubleExpression mapDetail(FormulaVarDet det, CalcContext context) {
        MeteringPoint meteringPoint = det.getMeteringPoint();
        Parameter param = det.getParam();
        ParamTypeEnum paramType = det.getParamType();

        if (meteringPoint.getMeteringPointTypeId().equals(2L)) {
            Formula formula = meteringPoint.getFormulas()
                .stream()
                .filter(t -> t.getParam().equals(param))
                .filter(t -> t.getParamType() == paramType)
                .findFirst()
                .orElse(null);

            if (formula != null) {
                logger.trace("nested formula");
                logger.trace("formulaId: " + formula.getId());
                logger.trace("pointCode: " + formula.getMeteringPoint().getCode());
                logger.trace("param: " + formula.getParam().getCode());
                logger.trace("paramType: " + formula.getParamType());
                return buildExpression(formula, context);
            }
        }

        if (context.isMeteringReading()) {
            logger.trace("context: mr");
            Double sign = det.getSign().equals("-") ? -1d : 1d;

            if (param.getCode().equals("WL")) {
                if (context.getTransformerValues() != null) {
                    if (context.getTransformerValues().containsKey(meteringPoint.getCode())) {
                        logger.trace("expression: DoubleValueExpression");
                        return DoubleValueExpression.builder()
                            .value(context.getTransformerValues().get(meteringPoint.getCode()))
                            .build();
                    }
                }

                logger.trace("expression: TransformerValueExpression");
                return TransformerValueExpression.builder()
                    .meteringPointCode(meteringPoint.getCode())
                    .parameterCode(param.getCode())
                    .rate(det.getRate() * sign)
                    .context(context)
                    .service(transformerValueService)
                    .build();
            }

            logger.trace("expression: MrExpression");
            return MrExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(det.getRate() * sign)
                .context(context)
                .service(mrService)
                .build();
        }

        if (context.isAsp()) {
            logger.trace("context: asp");
            Double sign = det.getSign().equals("-") ? -1d : 1d;

            logger.trace("expression: AspExpression");
            return AspExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(det.getRate() * sign)
                .context(context)
                .service(aspService)
                .build();
        }

        if (paramType == ParamTypeEnum.PT) {
            logger.trace("expression: PeriodTimeValueExpression");
            return PeriodTimeValueExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .periodType(context.getPeriodType())
                .rate(det.getRate())
                .startHour((byte) 0)
                .endHour((byte) 23)
                .service(periodTimeValueService)
                .context(context)
                .build();
        }

        if (paramType == ParamTypeEnum.AT || paramType == ParamTypeEnum.ATS || paramType == ParamTypeEnum.ATE) {
            logger.trace("expression: AtTimeValueExpression");
            String per = "end";
            if (paramType == ParamTypeEnum.ATS)
                per = "start";

            if (paramType == ParamTypeEnum.ATE)
                per = "end";

            return AtTimeValueExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .per(per)
                .rate(det.getRate())
                .service(atTimeValueService)
                .context(context)
                .build();
        }

        return DoubleValueExpression.builder().build();
    }
}
