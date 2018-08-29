package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.FormulaTypeEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcResult;
import calc.formula.CalcContext;
import calc.formula.exception.CycleDetectionException;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.StringExpression;
import calc.formula.expression.impl.*;
import calc.formula.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.script.ScriptEngine;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("Duplicates")
@Service
@RequiredArgsConstructor
public class CalcServiceImpl implements CalcService {
    private final ExpressionService expressionService;
    private final PeriodTimeValueService periodTimeValueService;
    private final AtTimeValueService atTimeValueService;
    private final BsResultMrService mrService;
    private final OperatorFactory operatorFactory;
    private final ScriptEngine engine;

    public CalcResult calcStr(String text, CalcContext context) throws Exception {
        Formula formula = new Formula();
        formula.setText(text);

        DoubleExpression expression = expressionService.parse(formula, context);
        return calcExpression(expression);
    }

    @Override
    public CalcResult calcExpression(DoubleExpression expression) {
        CalcResult result = new CalcResult();
        result.setDoubleValue(expression.doubleValue());
        result.setDoubleValues(expression.doubleValues());

        if (expression instanceof StringExpression) {
            StringExpression stringExpression = (StringExpression) expression;
            result.setStringValue(stringExpression.stringValue());
        }

        return result;
    }

    @Override
    public List<CalcResult> calcFormulas(List<Formula> formulas, CalcContext context) throws CycleDetectionException {
        Map<String, Formula> formulaMap = new HashMap<>();
        Map<String, DoubleExpression> expressionMap = new HashMap<>();
        Map<String, Set<String>> codesMap = new HashMap<>();
        for (Formula formula : formulas) {
            formulaMap.putIfAbsent(formula.getMeteringPoint().getCode(), formula);
            DoubleExpression expression = buildExpression(formula, context);
            expressionMap.putIfAbsent(formula.getMeteringPoint().getCode(), expression);
            codesMap.putIfAbsent(formula.getMeteringPoint().getCode(), expression.pointCodes());
        }

        List<CalcResult> results = new ArrayList<>();
        try {
            List<String> pointCodes = expressionService.sort(codesMap);
            for (String pointCode : pointCodes) {
                System.out.println(pointCode);

                DoubleExpression expression = expressionMap.get(pointCode);
                Formula formula = formulaMap.get(pointCode);

                CalcResult result = new CalcResult();
                result.setMeteringDate(context.getEndDate().atStartOfDay().plusDays(1));
                result.setMeteringPoint(formula.getMeteringPoint());
                result.setParam(formula.getParam());
                result.setUnit(formula.getParam().getUnit());
                result.setParamType(formula.getParamType().name());

                if (formula.getParamType() == ParamTypeEnum.AT) {
                    result.setDoubleValue(expression.doubleValue());
                    result.setPeriodType(context.getPeriodType());
                }

                if (formula.getParamType() == ParamTypeEnum.PT) {
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


    @Override
    public List<CalcResult> calcMeteringPoints(List<MeteringPoint> points, CalcContext context) throws CycleDetectionException {
        Set<String> set = new HashSet<>();
        List<MeteringPoint> allPoints = points.stream()
            .flatMap(t -> getChildPoints(t, set).stream())
            .collect(toList());

        allPoints.addAll(points);

        List<Formula> formulas = allPoints.stream()
            .flatMap(p -> p.getFormulas().stream())
            .collect(Collectors.toList());

        List<CalcResult> results = calcFormulas(formulas, context);

        return results.stream()
            .filter(r -> points.stream().filter(t -> t.getCode().equals(r.getMeteringPoint().getCode())).findFirst().isPresent())
            .collect(toList());
    }

    private Set<MeteringPoint> getChildPoints(MeteringPoint parentPoint, Set<String> set) {
        Set<MeteringPoint> points = parentPoint.getFormulas().stream()
            .flatMap(f -> f.getVars().stream())
            .flatMap(v -> v.getDetails().stream())
            .map(d -> d.getMeteringPoint())
            .collect(toSet());

        for (MeteringPoint point : points) {
            set.add(parentPoint.getCode() + "#" + point.getCode());
            if (set.contains(point.getCode() + "#" + parentPoint.getCode()))
                continue;

            points.addAll(getChildPoints(point, set));
        }

        return points;
    }

    private void cacheResult(CalcContext context, CalcResult cacheResult) {
        String code = cacheResult.getMeteringPoint().getCode();
        if (cacheResult.getParamType().equals("AT")) {
            List<CalcResult> values = context.getValues().getOrDefault(code, new ArrayList<>());
            values.add(cacheResult);
            context.getValues().put(code, values);
        }

        if (cacheResult.getParamType().equals("PT")) {
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
    @Override
    public DoubleExpression buildExpression(Formula formula, CalcContext context) {
        if (formula.getFormulaType() != FormulaTypeEnum.DIALOG)
            return DoubleValueExpression.builder().build();

        Map<String, DoubleExpression> attrs = new HashMap<>();
        for (FormulaVar var : formula.getVars())
            attrs.putIfAbsent(var.getVarName(), mapVar(var, context));

        return JsExpression.builder()
            .src(formula.getText())
            .attributes(attrs)
            .engine(engine)
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
        if (det.getIsMeteringReading()) {
            if (det.getParamType() == ParamTypeEnum.PT) {
                return MeteringReadingExpression.builder()
                    .meteringPointCode(det.getMeteringPoint().getCode())
                    .parameterCode(det.getParam().getCode())
                    .rate(1d)
                    .context(context)
                    .service(mrService)
                    .build();
            }
        }

        if (!det.getIsMeteringReading()) {
            if (det.getParamType() == ParamTypeEnum.PT) {
                return PeriodTimeValueExpression.builder()
                    .meteringPointCode(det.getMeteringPoint().getCode())
                    .parameterCode(det.getParam().getCode())
                    .periodType(context.getPeriodType())
                    .rate(det.getRate())
                    .startHour((byte) 0)
                    .endHour((byte) 23)
                    .service(periodTimeValueService)
                    .context(context)
                    .build();
            }

            if (det.getParamType() == ParamTypeEnum.AT || det.getParamType() == ParamTypeEnum.ATS || det.getParamType() == ParamTypeEnum.ATE) {
                String per = "end";
                if (det.getParamType() == ParamTypeEnum.ATS)
                    per = "start";

                if (det.getParamType() == ParamTypeEnum.ATE)
                    per = "end";

                return AtTimeValueExpression.builder()
                    .meteringPointCode(det.getMeteringPoint().getCode())
                    .parameterCode(det.getParam().getCode())
                    .per(per)
                    .rate(det.getRate())
                    .service(atTimeValueService)
                    .context(context)
                    .build();
            }
        }

        return DoubleValueExpression.builder().build();
    }
}
