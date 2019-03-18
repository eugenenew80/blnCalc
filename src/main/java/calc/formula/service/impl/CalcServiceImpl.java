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
    private final ReactorValueService reactorValueService;
    private final AspResultService aspService;
    private final SegResultService segService;
    private final InterResultMrService interMrService;
    private final ParamService paramService;
    private final WorkingHoursService workingHoursService;
    private final ReactorService reactorService;
    private final PowerTransformerService powerTransformerService;
    private final PowerLineService powerLineService;
    private final DistributionService distributionService;
    private final BalanceSubstResultUService uAvgService;
    private final OperatorFactory operatorFactory;
    private final ScriptEngine engine;


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
        logger.trace("startDate: "              + context.getHeader().getStartDate());
        logger.trace("endDate: "                + context.getHeader().getEndDate());
        logger.trace("processOrder: "           + property.getProcessOrder());
        logger.trace("formulaBehaviour: "       + context.getFormulaBehaviour());
        logger.trace("useDataTypePriority: "    + context.isUseDataTypePriority());
        logger.trace("nullPermissible: "        + context.isNullPermissible());
        logger.trace("traceEnabled: "           + context.isTraceEnabled());

        CalcResult result = null;
        if (point.getPointType() == PointTypeEnum.PMP || property.getProcessOrder() == ProcessOrderEnum.READ)
            result = readValue(point, param, context, property);
        else {
            if (property.getProcessOrder() == ProcessOrderEnum.CALC) {
                List<Formula> formulas = findFormulas(point, param, property.getParamType());
                Formula formula = getFormula(formulas, context);
                result = calcFormula(formula, context, property);
            }

            if (property.getProcessOrder() == ProcessOrderEnum.CALC_READ) {
                List<Formula> formulas = findFormulas(point, param, property.getParamType());
                Formula formula = getFormula(formulas, context);
                result = calcFormula(formula, context, property);
                if (result == null || result.getDoubleValue() == null)
                    result = readValue(point, param, context, property);
            }

            if (property.getProcessOrder() == ProcessOrderEnum.READ_CALC) {
                result = readValue(point, param, context, property);
                if (result == null || result.getDoubleValue() == null) {
                    List<Formula> formulas = findFormulas(point, param, property.getParamType());
                    Formula formula = getFormula(formulas, context);
                    result = calcFormula(formula, context, property);
                }
            }
        }

        if (!context.isNullPermissible())
            if (result == null || result.getDoubleValue() == null)
                throw new ValueNotFoundException("Не удалось определить значение параметра");

        if (context.isTraceEnabled())
            printCalcTrace(context);

        logger.trace("---------------------------------------------");
        return result;
    }

    private void printCalcTrace(CalcContext context) {
        for (String mpCode: context.getTraces().keySet()) {
            logger.trace("--trace----------------------------------");
            logger.trace("  mp: " + mpCode);
            List<CalcTrace> traces = context.getTraces().get(mpCode);
            for (CalcTrace trace : traces) {
                logger.trace("  param: " + trace.getParameterCode());
                if (trace.getSourceSystem() != null) {
                    logger.trace("  source system: " + trace.getSourceSystems());
                    logger.trace("  selected source system: " + trace.getSourceSystem());
                }
                if (trace.getDataType() != null) {
                    logger.trace("  data statuses: " + trace.getDataTypes());
                    logger.trace("  selected data status: " + trace.getDataType());
                }
                if (trace.getSource() != null) {
                    logger.trace("  sources: " + trace.getSources());
                    logger.trace("  selected source: " + trace.getSource());
                }
            }
            logger.trace("-----------------------------------------");
        }
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
        if (formula != null) {
            logger.trace("---------------------------------------------");
            logger.trace("point: " + formula.getMeteringPoint().getCode());
            logger.trace("pointType: " + formula.getMeteringPoint().getPointType());
            logger.trace("param: " + formula.getParam().getCode());
            logger.trace("paramType: " + property.getParamType());
            logger.trace("periodType: " + context.getHeader().getPeriodType());
            logger.trace("dataType: " + context.getHeader().getDataType());
            logger.trace("startDate: " + context.getHeader().getStartDate());
            logger.trace("endDate: " + context.getHeader().getEndDate());
            logger.trace("processOrder: " + property.getProcessOrder());
            logger.trace("formulaBehaviour: " + context.getFormulaBehaviour());
            logger.trace("useDataTypePriority: " + context.isUseDataTypePriority());
            logger.trace("nullPermissible: " + context.isNullPermissible());
            logger.trace("traceEnabled: " + context.isTraceEnabled());
        }
        return calcFormula(formula, context, property);
    }

    private CalcResult calcFormula(Formula formula, CalcContext context, CalcProperty property) {
        context.getTraces().clear();

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
        context.getTraces().clear();

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
            DoubleExpression expression1 = getExpression(point, paramService.getParam("A+"), 1d, context, property);
            DoubleExpression expression2 = getExpression(point, paramService.getParam("A-"), 1d, context, property);
            expression = BinaryExpression.builder()
                .operator(operatorFactory.binary("subtract"))
                .expressions(Arrays.asList(expression1, expression2))
                .build();
        }

        if (param.getCode().equals("RB")) {
            DoubleExpression expression1 = getExpression(point, paramService.getParam("R+"), 1d, context, property);
            DoubleExpression expression2 = getExpression(point, paramService.getParam("R-"), 1d, context, property);
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
        for (FormulaVar var : formula.getVars()) {
            DoubleExpression varExpression = mapVar(var, context, property);
            attrs.putIfAbsent(var.getVarName(), varExpression);
            logger.trace("  " + formula.getText() + ", " + var.getVarName() + " = " + varExpression.doubleValue());
        }

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
            Formula formula = formulas != null && !formulas.isEmpty() ? formulas.get(0) : null;

            if (formula != null) {
                logger.trace("for var " + det.getFormulaVar().getVarName() + " nested formula start");
                logger.trace("  point: " + formula.getMeteringPoint().getCode());
                logger.trace("  pointType: " + formula.getMeteringPoint().getPointType().name());
                logger.trace("  param: " + formula.getParam().getCode());
                logger.trace("  paramType: " + formula.getParamType());
                logger.trace("  formulaId: " + formula.getId());
                DoubleExpression expression = buildExpression(formula, context, property);
                logger.trace("nested formula end");

                UnaryOperator<DoubleExpression> signOperator = det.getSign().equals("-")
                    ? operatorFactory.unary("minus")
                    : operatorFactory.unary("nothing");

                UnaryOperator<DoubleExpression> roundOperator = formula.getParam().getDigitsRounding() !=0
                    ? operatorFactory.unary("round-" + formula.getParam().getDigitsRounding())
                    : operatorFactory.unary("round");

                return expression.andThen(signOperator)
                    .andThen(roundOperator);
            }
        }

        if (det.getMeteringPoint().getPointType() == PointTypeEnum.PMP && det.getParam().equals(paramService.getParam("AB"))) {
            DoubleExpression  expression1 = getExpression(det, paramService.getParam("A+"), context, property);
            DoubleExpression  expression2 = getExpression(det, paramService.getParam("A-"), context, property);
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

    private DoubleExpression getExpression(MeteringPoint mp, Parameter param, Double rate, CalcContext ctx, CalcProperty prop) {
        logger.trace("end point: " + mp.getCode());
        logger.trace("  param: " + param.getCode());
        logger.trace("  pointType: " + mp.getPointType());
        logger.trace("  paramType: " + prop.getParamType());
        logger.trace("  defContextType: " + ctx.getDefContextType());
        logger.trace("  contextType: " + prop.getContextType());
        logger.trace("  rate: " + rate);

        if (prop.getDeterminingMethod() == DeterminingMethodEnum.RDV) {
            if (prop.getGridType() == GridTypeEnum.OWN) {
                logger.trace("  expression: DistributionExpression: own");
                DistributionExpression.builder()
                    .meteringPointCode(mp.getCode())
                    .parameterCode(param.getCode())
                    .gridType(GridTypeEnum.OWN)
                    .electricityGroupId(prop.getElectricityGroup() != null ? prop.getElectricityGroup().getId() : null)
                    .service(distributionService)
                    .context(ctx)
                    .build();
            }

            if (prop.getGridType() == GridTypeEnum.OTHER) {
                logger.trace("  expression: DistributionExpression: other");
                DistributionExpression.builder()
                    .meteringPointCode(mp.getCode())
                    .parameterCode(param.getCode())
                    .gridType(GridTypeEnum.OTHER)
                    .electricityGroupId(prop.getElectricityGroup() != null ? prop.getElectricityGroup().getId() : null)
                    .service(distributionService)
                    .context(ctx)
                    .build();
            }

            if (prop.getGridType() == GridTypeEnum.TOTAL) {
                logger.trace("  expression: DistributionExpression: total");
                DistributionExpression.builder()
                    .meteringPointCode(mp.getCode())
                    .parameterCode(param.getCode())
                    .gridType(GridTypeEnum.TOTAL)
                    .electricityGroupId(prop.getElectricityGroup() != null ? prop.getElectricityGroup().getId() : null)
                    .service(distributionService)
                    .context(ctx)
                    .build();
            }
        }

        if (param.getCode().equals("U") && ctx.getDefContextType() == ContextTypeEnum.MR) {
            logger.trace("  expression: UavgExpression");
            return UavgExpression.builder()
                .meteringPointCode(mp.getCode())
                .def(mp.getVoltageClass() != null ? mp.getVoltageClass().getValue() / 1000d : 0d)
                .context(ctx)
                .service(uAvgService)
                .build();
        }

        if (param.getCode().equals("WL") && ctx.getDefContextType() == ContextTypeEnum.MR) {
            Map<String, Double> cachedValues = ctx.getTransformerValues();
            if (cachedValues != null && cachedValues.containsKey(mp.getCode())) {
                logger.trace("  expression: CachedValueExpression");
                return DoubleValueExpression.builder()
                    .value(cachedValues.get(mp.getCode()))
                    .build();
            }

            logger.trace("  expression: TransformerValueExpression");
            DoubleExpression expression = TransformerValueExpression.builder()
                .meteringPointCode(mp.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(ctx)
                .service(transformerValueService)
                .build();

            if (expression.doubleValue() == null) {
                logger.trace("  expression: ReactorValueExpression");
                return ReactorValueExpression.builder()
                    .meteringPointCode(mp.getCode())
                    .parameterCode(param.getCode())
                    .rate(rate)
                    .context(ctx)
                    .service(reactorValueService)
                    .build();
            }

            return expression;
        }

        if (prop.getContextType() == ContextTypeEnum.MR || (ctx.getDefContextType() == ContextTypeEnum.MR && mp.getPointType() == PointTypeEnum.PMP)) {
            logger.trace("  ctx: mr");
            logger.trace("  expression: MrExpression");
            return MrExpression.builder()
                .meteringPointCode(mp.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(ctx)
                .service(mrService)
                .build();
        }

        if (prop.getContextType() == ContextTypeEnum.ASP) {
            if ((param.getCode().equals("A+") || param.getCode().equals("A-") || param.getCode().equals("R+") || param.getCode().equals("R-") )) {
                logger.trace("  ctx: asp");
                logger.trace("  expression: AspExpression");
                AspExpression expression = AspExpression.builder()
                    .meteringPointCode(mp.getCode())
                    .parameterCode(param.getCode())
                    .rate(rate)
                    .context(ctx)
                    .service(aspService)
                    .build();

                return expression;
            }
        }

        if (prop.getContextType() == ContextTypeEnum.SEG) {
            logger.trace("  ctx: seg");
            logger.trace("  expression: SegExpression");
            return SegExpression.builder()
                .meteringPointCode(mp.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(ctx)
                .service(segService)
                .build();
        }

        if (prop.getContextType() == ContextTypeEnum.INTER_MR) {
            logger.trace("  ctx: inter");
            logger.trace("  expression: InterExpression");
            return InterMrExpression.builder()
                .meteringPointCode(mp.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .context(ctx)
                .service(interMrService)
                .build();
        }

        if (prop.getParamType() == ParamTypeEnum.PT) {
            logger.trace("  expression: PeriodTimeValueExpression");
            PeriodTimeValueExpression ptExpression = PeriodTimeValueExpression.builder()
                .meteringPointCode(mp.getCode())
                .parameterCode(param.getCode())
                .rate(rate)
                .service(periodTimeValueService)
                .context(ctx)
                .build();

            if (prop.getContextType() != ctx.getDefContextType()) {
                Double val = ptExpression.doubleValue();
                if (val == null && ctx.getDefContextType() == ContextTypeEnum.MR) {
                    logger.trace("  ctx: mr");
                    logger.trace("  expression: MrExpression");
                    return MrExpression.builder()
                        .meteringPointCode(mp.getCode())
                        .parameterCode(param.getCode())
                        .rate(rate)
                        .context(ctx)
                        .service(mrService)
                        .build();
                }
            }

            return ptExpression;
        }

        if (prop.getParamType() == ParamTypeEnum.AT || prop.getParamType() == ParamTypeEnum.ATS || prop.getParamType() == ParamTypeEnum.ATE) {
            logger.trace("  expression: AtTimeValueExpression");
            String per = "end";
            if (prop.getParamType() == ParamTypeEnum.ATS)
                per = "start";

            if (prop.getParamType() == ParamTypeEnum.ATE)
                per = "end";

            return AtTimeValueExpression.builder()
                .meteringPointCode(mp.getCode())
                .parameterCode(param.getCode())
                .per(per)
                .rate(rate)
                .service(atTimeValueService)
                .context(ctx)
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
