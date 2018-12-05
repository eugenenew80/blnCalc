package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.*;
import calc.formula.*;
import calc.formula.exception.CycleDetectionException;
import calc.formula.exception.FormulaNotFoundException;
import calc.formula.exception.TooManyFormulasException;
import calc.formula.exception.ValueNotFoundException;
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
import java.util.function.UnaryOperator;
import static calc.util.Util.round;
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
    private final WorkingHoursService workingHoursService;
    private final ReactorService reactorService;
    private final PowerTransformerService powerTransformerService;
    private final PowerLineService powerLineService;
    private final DistributionService distributionService;
    private final OperatorFactory operatorFactory;
    private final ScriptEngine engine;
    private Map<String, Parameter> mapParams = null;

    @PostConstruct
    public void init() { mapParams = paramService.getValues(); }

    @Override
    public CalcResult calcValue(MeteringPoint point, Parameter param, CalcContext context) {
        CalcProperty property = CalcProperty.builder()
            .contextType(context.getDefContextType())
            .determiningMethod(DeterminingMethodEnum.MPV)
            .paramType(ParamTypeEnum.PT)
            .build();

        return calcValue(point, param, context, property);
    }

    @Override
    public CalcResult calcValue(MeteringPoint point, Parameter param, CalcContext context, CalcProperty property) {
        if (point == null || param == null || context == null || context.getHeader() == null)
            return null;

        logger.trace("---------------------------------------------");
        logger.trace("point: "                  + point.getCode());
        logger.trace("pointType: "              + point.getPointType());
        logger.trace("param: "                  + param.getCode());
        logger.trace("paramType: "              + property.getParamType());
        logger.trace("periodType: "             + context.getHeader().getPeriodType());
        logger.trace("dataType: "               + context.getHeader().getDataType());
        logger.trace("processOrder: "           + property.getProcessOrder());
        logger.trace("formulaBehaviour: "       + context.getFormulaBehaviour());
        logger.trace("useDataTypePriority: "    + context.isUseDataTypePriority());
        logger.trace("nullPermissible: "        + context.isNullPermissible());
        logger.trace("traceEnabled: "           + context.isTraceEnabled());

        CalcResult result = null;
        if (point.getPointType() == PointTypeEnum.PMP)
            result = readValue(point, param, context, property);

        if (property.getProcessOrder() == ProcessOrderEnum.READ)
            result = readValue(point, param, context, property);

        List<Formula> formulas = findFormulas(point, param, property.getParamType());

        if (property.getProcessOrder() == ProcessOrderEnum.CALC) {
            Formula formula = getFormula(formulas, context);
            result = calcValue(formula, context, property);
        }

        if (property.getProcessOrder() == ProcessOrderEnum.CALC_READ) {
            Formula formula = getFormula(formulas, context);
            result = calcValue(formula, context, property);
            if (result == null || result.getDoubleValue() == null)
                result = readValue(point, param, context, property);
        }

        if (property.getProcessOrder() == ProcessOrderEnum.READ_CALC) {
            result = readValue(point, param, context, property);
            if (result == null || result.getDoubleValue() == null) {
                Formula formula = getFormula(formulas, context);
                result = calcValue(formula, context, property);
            }
        }

        if (!context.isNullPermissible())
            if (result == null || result.getDoubleValue() == null)
                throw new ValueNotFoundException("Не удалось определить значение параметра");

        logger.trace("---------------------------------------------");
        return result;
    }

    private Formula getFormula(List<Formula> formulas, CalcContext context) {
        if (context.getFormulaBehaviour() == FormulaBehaviourEnum.EXACTLY_ONE || context.getFormulaBehaviour() == FormulaBehaviourEnum.ZERO_OR_ONE)
            if (formulas.size() > 1)
                throw new TooManyFormulasException("Невозможно определить значение по формуле: назначено больше одной формулы");

        if (context.getFormulaBehaviour() == FormulaBehaviourEnum.EXACTLY_ONE)
            if (formulas.isEmpty())
                throw new FormulaNotFoundException("Невозможно определить значение по формуле: не найдено ни одной формулы");

        Formula formula = formulas!=null && !formulas.isEmpty() ? formulas.get(0) : null;
        if (formula == null)
            logger.warn("Formula not found");
        else
            logger.trace("Formula found, id: " + formula.getId());

        return formula;
    }

    @Override
    public CalcResult calcValue(Formula formula, CalcContext context) {
        CalcProperty property = CalcProperty.builder()
            .contextType(context.getDefContextType())
            .determiningMethod(DeterminingMethodEnum.MPV)
            .paramType(formula.getParamType())
            .build();

        return calcValue(formula, context, property);
    }

    @Override
    public CalcResult calcValue(Formula formula, CalcContext context, CalcProperty property) {
        if (formula == null)
            return null;

        detectCycles(formula);

        DoubleExpression expression = buildExpression(formula, context, property);
        CalcResult result = new CalcResult();
        result.setFormula(formula);
        result.setMeteringDate(context.getHeader().getEndDate().atStartOfDay().plusDays(1));
        result.setMeteringPoint(formula.getMeteringPoint());
        result.setParam(formula.getParam());
        result.setUnit(formula.getParam().getUnit());
        result.setParamType(formula.getParamType().name());

        if (context.getHeader().getPeriodType() != PeriodTypeEnum.H) {
            Double doubleValue = expression.doubleValue();
            if (doubleValue != null && Double.isNaN(doubleValue))
                doubleValue = null;

            doubleValue = round(doubleValue, formula.getParam());
            result.setDoubleValue(doubleValue);
            result.setPeriodType(context.getHeader().getPeriodType());
        }

        if (context.getHeader().getPeriodType() == PeriodTypeEnum.H) {
            result.setDoubleValues(expression.doubleValues());
            result.setPeriodType(context.getHeader().getPeriodType());
        }

        logger.trace("result:");
        logger.trace("  formulaId: " + result.getFormula().getId());
        logger.trace("  src: " + result.getFormula().getText());
        logger.trace("  point: " + result.getMeteringPoint().getCode());
        logger.trace("  param: " + result.getParam().getCode());
        logger.trace("  paramType: " + result.getParamType());
        logger.trace("  meteringDate: " + result.getMeteringDate());
        logger.trace("  periodType: " + result.getPeriodType());

        if (result.getPeriodType() != PeriodTypeEnum.H)
            logger.trace("  value: " + result.getDoubleValue());

        if (result.getPeriodType() == PeriodTypeEnum.H)
            logger.trace("  values: " + Arrays.deepToString(result.getDoubleValues()));

        if (context.getException() != null)
            throw context.getException();

        return result;
    }


    @Override
    public CalcResult readValue(MeteringPoint point, Parameter param, CalcContext context, CalcProperty property) {
        DoubleExpression expression = DoubleValueExpression.builder()
            .value(null)
            .build();

        if (point.getPointType() == PointTypeEnum.PMP)
            expression = param.getCode().equals("AB") || param.getCode().equals("RB")
                ? getComplexExpression(point, param, context, property)
                : getExpression(point, param, 1d, context, property);

        if (point.getPointType() == PointTypeEnum.VMP)
            expression = getExpression(point, param, 1d, context, property);

        CalcResult result = new CalcResult();
        result.setMeteringDate(context.getHeader().getEndDate().atStartOfDay().plusDays(1));
        result.setMeteringPoint(point);
        result.setParam(param);
        result.setUnit(param.getUnit());
        result.setParamType(property.getParamType().name());

        if (context.getHeader().getPeriodType() != PeriodTypeEnum.H) {
            result.setDoubleValue(expression.doubleValue());
            result.setPeriodType(context.getHeader().getPeriodType());
            logger.trace("  val: " + expression.doubleValue());
        }

        if (context.getHeader().getPeriodType() == PeriodTypeEnum.H) {
            result.setDoubleValues(expression.doubleValues());
            result.setPeriodType(context.getHeader().getPeriodType());
            logger.trace("  val: " + Arrays.deepToString(expression.doubleValues()));
        }
        return result;
    }

    private DoubleExpression getComplexExpression(MeteringPoint point, Parameter param, CalcContext context, CalcProperty property) {
        DoubleExpression expression = DoubleValueExpression.builder()
            .value(null)
            .build();

        if (param.getCode().equals("AB")) {
            DoubleExpression expression1 = getExpression(point, mapParams.get("A+"), 1d, context, property);
            DoubleExpression expression2 = getExpression(point, mapParams.get("A-"), 1d, context, property);
            expression = BinaryExpression.builder()
                .operator(operatorFactory.binary("subtract"))
                .expressions(Arrays.asList(expression1, expression2))
                .build();
        }

        if (param.getCode().equals("RB")) {
            DoubleExpression expression1 = getExpression(point, mapParams.get("R+"), 1d, context, property);
            DoubleExpression expression2 = getExpression(point, mapParams.get("R-"), 1d, context, property);
            expression = BinaryExpression.builder()
                .operator(operatorFactory.binary("subtract"))
                .expressions(Arrays.asList(expression1, expression2))
                .build();
        }

        return expression;
    }

    private List<Formula> findFormulas(MeteringPoint point, Parameter param, ParamTypeEnum paramType) {
        if (point.getPointType() != PointTypeEnum.VMP)
            return null;

        List<Formula> formulas = point.getFormulas()
            .stream()
            .filter(t -> t.getParam().equals(param))
            .filter(t -> t.getParamType() == paramType)
            .collect(toList());

        return formulas;
    }

    private DoubleExpression buildExpression(Formula formula, CalcContext context, CalcProperty property) {
        if (formula.getFormulaType() != FormulaTypeEnum.DIALOG || formula.getText() == null)
            return DoubleValueExpression.builder().build();

        Map<String, DoubleExpression> attrs = new HashMap<>();
        for (FormulaVar var : formula.getVars())
            attrs.putIfAbsent(var.getVarName(), mapVar(var, context, property));

        return JsExpression.builder()
            .src(formula.getText())
            .attributes(attrs)
            .engine(engine)
            .context(context)
            .build();
    }

    private DoubleExpression mapVar(FormulaVar var, CalcContext context, CalcProperty property) {
        List<DoubleExpression> expressions = null;
        if (var.getVarType() == VarTypeEnum.MP) {
            expressions = var.getDetails()
                .stream()
                .map(t -> mapDetail(t, context, property))
                .filter(t -> t != null)
                .collect(toList());
        }

        if (var.getVarType() == VarTypeEnum.EQ) {
            expressions = var.getEquipments()
                .stream()
                .map(t -> mapEquipment(t, context))
                .filter(t -> t != null)
                .collect(toList());
        }

        if (expressions != null) {
            if (expressions.size() == 0) return DoubleValueExpression.builder().build();
            if (expressions.size() == 1) return expressions.get(0);
            return BinaryExpression.builder()
                .operator(operatorFactory.binary("add"))
                .expressions(expressions)
                .build();
        }

        return DoubleValueExpression.builder().build();
    }

    private DoubleExpression mapDetail(FormulaVarDet det, CalcContext context, CalcProperty property) {
        if (det.getMeteringPoint().getPointType() == PointTypeEnum.VMP) {
            List<Formula> formulas = findFormulas(det.getMeteringPoint(), det.getParam(), det.getParamType());
            Formula formula = !formulas.isEmpty() ? formulas.get(0) : null;

            if (formula != null) {
                logger.trace("nested formula start");
                logger.trace("  point: " + formula.getMeteringPoint().getCode());
                logger.trace("  pointType: " + formula.getMeteringPoint().getPointType().name());
                logger.trace("  param: " + formula.getParam().getCode());
                logger.trace("  paramType: " + formula.getParamType());
                logger.trace("  formulaId: " + formula.getId());
                DoubleExpression expression = buildExpression(formula, context, property);
                logger.trace("nested formula end");

                UnaryOperator<DoubleExpression> operator = det.getSign().equals("-")
                    ? operatorFactory.unary("minus")
                    : operatorFactory.unary("nothing");

                return expression.andThen(operator);
            }
        }

        if (det.getParam().equals(mapParams.get("AB"))) {
            DoubleExpression  expression1 = getExpression(det, mapParams.get("A+"), context, property);
            DoubleExpression  expression2 = getExpression(det, mapParams.get("A-"), context, property);
            return BinaryExpression.builder()
                .operator(operatorFactory.binary("subtract"))
                .expressions(Arrays.asList(expression1, expression2))
                .build();
        }

        return getExpression(det, det.getParam(), context, property);
    }

    private DoubleExpression mapEquipment(FormulaVarEq eq, CalcContext context) {
        if (eq.getEquipmentType() == EquipmentTypeEnum.R) {
            if (eq.getParam().getCode().equals("TW")) {
                return WorkingHoursExpression.builder()
                    .objectType("re")
                    .objectId(eq.getEquipmentId())
                    .context(context)
                    .service(workingHoursService)
                    .build();
            }

            String attr;
            switch (eq.getParam().getCode()) {
                case "UNOM":
                    attr = "unom";
                    break;
                case "PXX":
                    attr = "delta_pr";
                    break;
                default:
                    attr = null;
            }

            if (attr != null) {
                return ReactorExpression.builder()
                    .id(eq.getEquipmentId())
                    .attr(attr)
                    .context(context)
                    .service(reactorService)
                    .build();
            }
            return DoubleValueExpression.builder().build();
        }

        if (eq.getEquipmentType() == EquipmentTypeEnum.PT) {
            if (eq.getParam().getCode().equals("TW")) {
                return WorkingHoursExpression.builder()
                    .objectType("tr")
                    .objectId(eq.getEquipmentId())
                    .context(context)
                    .service(workingHoursService)
                    .build();
            }

            String attr;
            switch (eq.getParam().getCode()) {
                case "SNOM":
                    attr = "snom";
                    break;
                case "PXX":
                    attr = "delta_pxx";
                    break;
                case "UNOM_H":
                    attr = "unom_h";
                    break;
                case "PKZ_HL":
                    attr = "pkz_hl";
                    break;
                case "PKZ_HM":
                    attr = "pkz_hm";
                    break;
                case "PKZ_ML":
                    attr = "pkz_ml";
                    break;
                case "T2_R":
                    attr = "resist";
                    break;
                default:
                    attr = null;
            }

            if (attr != null) {
                return PowerTransformerExpression.builder()
                    .id(eq.getEquipmentId())
                    .attr(attr)
                    .context(context)
                    .service(powerTransformerService)
                    .build();
            }
            return DoubleValueExpression.builder().build();
        }

        if (eq.getEquipmentType() == EquipmentTypeEnum.PL) {
            String attr;
            switch (eq.getParam().getCode()) {
                case "L": attr = "snom";
                    break;
                case "B0":
                    attr = "po";
                    break;
                case "R0":
                    attr = "r";
                    break;
                default:
                    attr = null;
            }

            if (attr != null) {
                return PowerLineExpression.builder()
                    .id(eq.getEquipmentId())
                    .attr(attr)
                    .context(context)
                    .service(powerLineService)
                    .build();
            }
            return DoubleValueExpression.builder().build();
        }

        return DoubleValueExpression.builder().build();
    }

    private DoubleExpression getExpression(FormulaVarDet det, Parameter param, CalcContext context, CalcProperty property) {
        MeteringPoint meteringPoint = det.getMeteringPoint();
        Double sign = det.getSign().equals("-") ? -1d : 1d;
        Double rate = ofNullable(det.getRate()).orElse(1d) * sign;
        return getExpression(meteringPoint, param, rate, context, property);
    }

    private DoubleExpression getExpression(MeteringPoint meteringPoint, Parameter param, Double rate, CalcContext context, CalcProperty property) {
        logger.trace("end point: " + meteringPoint.getCode());
        logger.trace("  param: " + param.getCode());
        logger.trace("  pointType: " + meteringPoint.getPointType());
        logger.trace("  paramType: " + property.getParamType());
        logger.trace("  defContextType: " + context.getDefContextType());
        logger.trace("  contextType: " + property.getContextType());
        logger.trace("  rate: " + rate);

        if (property.getDeterminingMethod() == DeterminingMethodEnum.RDV) {
            if (property.getGridType() == GridTypeEnum.OWN) {
                logger.trace("  expression: DistributionExpression: own");
                DistributionExpression.builder()
                    .meteringPointCode(meteringPoint.getCode())
                    .parameterCode(param.getCode())
                    .gridType(GridTypeEnum.OWN)
                    .electricityGroupId(property.getElectricityGroup() != null ? property.getElectricityGroup().getId() : null)
                    .service(distributionService)
                    .context(context)
                    .build();
            }

            if (property.getGridType() == GridTypeEnum.OTHER) {
                logger.trace("  expression: DistributionExpression: other");
                DistributionExpression.builder()
                    .meteringPointCode(meteringPoint.getCode())
                    .parameterCode(param.getCode())
                    .gridType(GridTypeEnum.OTHER)
                    .electricityGroupId(property.getElectricityGroup() != null ? property.getElectricityGroup().getId() : null)
                    .service(distributionService)
                    .context(context)
                    .build();
            }

            if (property.getGridType() == GridTypeEnum.TOTAL) {
                logger.trace("  expression: DistributionExpression: total");
                DistributionExpression.builder()
                    .meteringPointCode(meteringPoint.getCode())
                    .parameterCode(param.getCode())
                    .gridType(GridTypeEnum.TOTAL)
                    .electricityGroupId(property.getElectricityGroup() != null ? property.getElectricityGroup().getId() : null)
                    .service(distributionService)
                    .context(context)
                    .build();
            }
        }

        if (param.getCode().equals("WL") && context.getDefContextType() == ContextTypeEnum.MR) {
            Map<String, Double> transformerValues = context.getTransformerValues();
            if (transformerValues != null && transformerValues.containsKey(meteringPoint.getCode())) {
                logger.trace("  expression: CachedValueExpression");
                return DoubleValueExpression.builder()
                    .value(transformerValues.get(meteringPoint.getCode()))
                    .build();
            }

            logger.trace("  expression: TransformerValueExpression");
            return TransformerValueExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(transformerValueService)
                .build();
        }

        if (property.getContextType() == ContextTypeEnum.MR) {
            logger.trace("  context: mr");
            logger.trace("  expression: MrExpression");
            return MrExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(mrService)
                .build();
        }

        if (property.getContextType() == ContextTypeEnum.ASP) {
            logger.trace("  context: asp");
            logger.trace("  expression: AspExpression");
            return AspExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(aspService)
                .build();
        }

        if (property.getContextType() == ContextTypeEnum.SEG) {
            logger.trace("  context: seg");
            logger.trace("  expression: SegExpression");
            return SegExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(segService)
                .build();
        }

        if (property.getContextType() == ContextTypeEnum.INTER) {
            logger.trace("  context: inter");
            logger.trace("  expression: InterExpression");
            return InterMrExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(context)
                .service(interMrService)
                .build();
        }

        if (property.getParamType() == ParamTypeEnum.PT) {
            logger.trace("  expression: PeriodTimeValueExpression");
            PeriodTimeValueExpression ptExpression = PeriodTimeValueExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param.getCode())
                .service(periodTimeValueService)
                .context(context)
                .build();

            if (property.getContextType() != context.getDefContextType()) {
                Double val = ptExpression.doubleValue();
                logger.trace("  val: " + val);

                if (val == null && context.getDefContextType() == ContextTypeEnum.MR) {
                    logger.trace("  context: mr");
                    logger.trace("  expression: MrExpression");
                    return MrExpression.builder()
                        .meteringPointCode(meteringPoint.getCode())
                        .parameterCode(param.getCode())
                        .rate(rate)
                        .context(context)
                        .service(mrService)
                        .build();
                }
            }

            return ptExpression;
        }

        if (property.getParamType() == ParamTypeEnum.AT || property.getParamType() == ParamTypeEnum.ATS || property.getParamType() == ParamTypeEnum.ATE) {
            logger.trace("  expression: AtTimeValueExpression");
            String per = "end";
            if (property.getParamType() == ParamTypeEnum.ATS)
                per = "start";

            if (property.getParamType() == ParamTypeEnum.ATE)
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
        buildGraph(formula, graph);

        CycleDetector<MeteringPoint, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        Set<MeteringPoint> detectedCycles = cycleDetector.findCycles();
        if (!detectedCycles.isEmpty())
            throw new CycleDetectionException("Cycles detected");
    }

    private void buildGraph(Formula rootFormula, DefaultDirectedGraph<MeteringPoint, DefaultEdge> graph) {
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
                    buildGraph(formula, graph);
            }
        }
    }
}
