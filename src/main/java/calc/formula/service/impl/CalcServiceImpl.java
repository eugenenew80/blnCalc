package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.FormulaTypeEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.entity.calc.enums.PointTypeEnum;
import calc.formula.CalcResult;
import calc.formula.CalcContext;
import calc.formula.ContextType;
import calc.formula.exception.CycleDetectionException;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.impl.*;
import calc.formula.service.*;
import lombok.RequiredArgsConstructor;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class CalcServiceImpl implements CalcService {
    private static final Logger logger = LoggerFactory.getLogger(CalcService.class);
    private final PeriodTimeValueService periodTimeValueService;
    private final AtTimeValueService atTimeValueService;
    private final BalanceSubstResultMrService mrService;
    private final TransformerValueService transformerValueService;
    private final AspResultService aspService;
    private final SegResultService segService;
    private final InterResultMrService interMrService;
    private final ParamService paramService;
    private final OperatorFactory operatorFactory;
    private final ScriptEngine engine;
    private Map<String, Parameter> mapParams = null;

    @PostConstruct
    public void init() {
        mapParams = paramService.getValues();
    }

    @Override
    public CalcResult calcMeteringPoint(MeteringPoint point, Parameter param, ParamTypeEnum paramType, CalcContext context) throws Exception {
        if (point == null || param == null || context == null)
            return null;

        logger.trace("---------------------------------------------");
        logger.trace("point: " + point.getCode());
        logger.trace("pointType: " + point.getPointType().name());
        logger.trace("param: " + param.getCode());
        logger.trace("paramType: " + paramType.name());
        logger.trace("periodType: " + context.getPeriodType());

        Formula formula = null;
        if (point.getPointType() == PointTypeEnum.VMP) {
            formula = point.getFormulas()
                .stream()
                .filter(t -> t.getParam().equals(param))
                .filter(t -> t.getParamType() == paramType)
                .findFirst()
                .orElse(null);

            if (formula == null) logger.warn("Formula not found");
        }

        if (formula == null) {
            DoubleExpression expression = getDefaultExpression(point, param, paramType, 1d, context);

            CalcResult result = new CalcResult();
            result.setMeteringDate(context.getEndDate().atStartOfDay().plusDays(1));
            result.setMeteringPoint(point);
            result.setParam(param);
            result.setUnit(param.getUnit());
            result.setParamType(paramType.name());

            if (context.getPeriodType() != PeriodTypeEnum.H) {
                result.setDoubleValue(expression.doubleValue());
                result.setPeriodType(context.getPeriodType());
                logger.trace("val: " + expression.doubleValue());
            }

            if (context.getPeriodType() == PeriodTypeEnum.H) {
                result.setDoubleValues(expression.doubleValues());
                result.setPeriodType(context.getPeriodType());
                logger.trace("val: " + Arrays.deepToString(expression.doubleValues()));
            }
            return result;
        }

        logger.trace("formulaId: " + formula.getId());
        CalcResult result = calcMeteringPoint(formula, context);

        logger.trace("---------------------------------------------");
        return result;
    }

    @Override
    public CalcResult calcMeteringPoint(Formula formula, CalcContext context) throws Exception {
        if (formula == null)
            return null;

        CalcResult result = calcFormula(formula, context);
        logger.trace("result:");
        logger.trace("formulaId: " + result.getFormula().getId());
        logger.trace("src: " + result.getFormula().getText());
        logger.trace("point: " + result.getMeteringPoint().getCode());
        logger.trace("param: " + result.getParam().getCode());
        logger.trace("paramType: " + result.getParamType());
        logger.trace("meteringDate: " + result.getMeteringDate());
        logger.trace("periodType: " + result.getPeriodType());

        if (result.getPeriodType() != PeriodTypeEnum.H)
            logger.trace("value: " + result.getDoubleValue());

        if (result.getPeriodType() == PeriodTypeEnum.H)
            logger.trace("values: " + Arrays.deepToString(result.getDoubleValues()));

        return result;
    }

    private CalcResult calcFormula(Formula formula, CalcContext context) throws Exception {
        detectCycles(formula);

        DoubleExpression expression = buildExpression(formula, context);
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
        return result;
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
        if (det.getMeteringPoint().getPointType() == PointTypeEnum.VMP) {
            Formula formula = det.getMeteringPoint()
                .getFormulas()
                .stream()
                .filter(t -> t.getParam().equals(det.getParam()))
                .filter(t -> t.getParamType() == det.getParamType())
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

        if (det.getParam().equals(mapParams.get("AB"))) {
            DoubleExpression  expression1 = getExpression(det, mapParams.get("A+"), context);
            DoubleExpression  expression2 = getExpression(det, mapParams.get("A-"), context);
            return BinaryExpression.builder()
                .operator(operatorFactory.binary("min"))
                .expressions(Arrays.asList(expression1, expression2))
                .build();
        }

        return getExpression(det, det.getParam(), context);
    }

    private DoubleExpression getExpression(FormulaVarDet det, Parameter param, CalcContext context) {
        MeteringPoint meteringPoint = det.getMeteringPoint();
        ParamTypeEnum paramType = det.getParamType();
        Double sign = det.getSign().equals("-") ? -1d : 1d;
        Double rate = ofNullable(det.getRate()).orElse(1d) * sign;

        if (param.getCode().equals("WL") && context.getContextType() == ContextType.MR) {
            Map<String, Double> transformerValues = context.getTransformerValues();
            if (transformerValues != null && transformerValues.containsKey(meteringPoint.getCode())) {
                logger.trace("expression: CachedValueExpression");
                return DoubleValueExpression.builder()
                    .value(transformerValues.get(meteringPoint.getCode()))
                    .build();
            }

            logger.trace("expression: TransformerValueExpression");
            return TransformerValueExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(transformerValueService)
                .build();
        }

        if (context.getContextType() == ContextType.MR) {
            logger.trace("context: mr");
            logger.trace("expression: MrExpression");
            return MrExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(mrService)
                .build();
        }

        if (context.getContextType() == ContextType.ASP) {
            logger.trace("context: asp");
            logger.trace("expression: AspExpression");
            return AspExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(aspService)
                .build();
        }

        if (context.getContextType() == ContextType.SEG) {
            logger.trace("context: seg");
            logger.trace("expression: SegExpression");
            return SegExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(segService)
                .build();
        }

        if (context.getContextType() == ContextType.INTER) {
            logger.trace("context: inter");
            logger.trace("expression: InterExpression");
            return InterMrExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(interMrService)
                .build();
        }

        return getDefaultExpression(meteringPoint, param, paramType, rate, context);
    }

    private DoubleExpression getDefaultExpression(MeteringPoint meteringPoint, Parameter param, ParamTypeEnum paramType, Double rate, CalcContext context) {
        if (paramType == ParamTypeEnum.PT) {
            logger.trace("expression: PeriodTimeValueExpression");
            return PeriodTimeValueExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .periodType(context.getPeriodType())
                .rate(rate)
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
                .rate(rate)
                .service(atTimeValueService)
                .context(context)
                .build();
        }

        return DoubleValueExpression.builder().build();
    }

    private void detectCycles(Formula formula) throws CycleDetectionException {
        DefaultDirectedGraph<MeteringPoint, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        fillGraph(formula, graph);

        CycleDetector<MeteringPoint, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        Set<MeteringPoint> detectedCycles = cycleDetector.findCycles();
        if (!detectedCycles.isEmpty())
            throw new CycleDetectionException("Cycles detected");
    }

    private void fillGraph(Formula rootFormula, DefaultDirectedGraph<MeteringPoint, DefaultEdge> graph) {
        if (rootFormula == null)
            return;

        if (!graph.containsVertex(rootFormula.getMeteringPoint()))
            graph.addVertex(rootFormula.getMeteringPoint());

        Set<FormulaVarDet> details = rootFormula.getVars()
            .stream()
            .flatMap(t -> t.getDetails().stream())
            .filter(t -> t.getMeteringPoint() != null)
            .collect(toSet());

        for (FormulaVarDet det : details) {
            if (!graph.containsVertex(det.getMeteringPoint()))
                graph.addVertex(det.getMeteringPoint());

            if (graph.containsEdge(rootFormula.getMeteringPoint(), det.getMeteringPoint()))
                continue;

            graph.addEdge(rootFormula.getMeteringPoint(), det.getMeteringPoint());

            if (det.getMeteringPoint().getPointType() == PointTypeEnum.VMP) {
                Formula formula = det.getMeteringPoint()
                    .getFormulas()
                    .stream()
                    .filter(t -> t.getParam().equals(det.getParam()))
                    .filter(t -> t.getParamType() == det.getParamType())
                    .findFirst()
                    .orElse(null);

                if (formula != null)
                    fillGraph(formula, graph);
            }
        }
    }
}
