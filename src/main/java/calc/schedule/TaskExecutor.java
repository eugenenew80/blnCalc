package calc.schedule;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.calculations.BalanceSubstMrService;
import calc.formula.calculations.BalanceSubstPeService;
import calc.formula.calculations.BalanceSubstUService;
import calc.formula.calculations.BalanceSubstUbService;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.impl.AtTimeValueExpression;
import calc.formula.expression.impl.BinaryExpression;
import calc.formula.expression.impl.UnaryExpression;
import calc.formula.service.AtTimeValueService;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.formula.service.OperatorFactory;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstMrService balanceSubstMrService;
    private final BalanceSubstUbService balanceSubstUbService;
    private final BalanceSubstUService balanceSubstUService;
    private final BalanceSubstPeService balanceSubstPeService;
    private final AtTimeValueService atTimeValueService;
    private final CalcService calcService;
    private final OperatorFactory operatorFactory;
    private final FormulaRepo formulaRepo;
    private final ExpressionService expressionService;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;

    @Scheduled(cron = "*/30 * * * * *")
    public void run() {
        CalcContext context = CalcContext.builder()
            .startDate(LocalDate.of(2018, Month.AUGUST, 13))
            .endDate(LocalDate.of(2018, Month.AUGUST, 13))
            .orgId(1l)
            .energyObjectType("SUBSTATION")
            .energyObjectId(1l)
            .trace(new HashMap<>())
            .values(new ArrayList<>())
            .build();


        Formula formula1 = formulaRepo.findOne(1l);
        String formulaStr1 = calcService.formulaToString(formula1);
        formula1.setText(formulaStr1);
        System.out.println(formulaStr1);

        Formula formula2 = formulaRepo.findOne(2l);
        String formulaStr2 = calcService.formulaToString(formula2);
        formula2.setText(formulaStr2);
        System.out.println(formulaStr2);

        DoubleExpression expression1 = null;
        DoubleExpression expression2 = null;
        try {
            expression1 = expressionService.parse(formula1, formula1.getParam().getCode(), context);
            expression2 = expressionService.parse(formula2, formula2.getParam().getCode(), context);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        List<DoubleExpression> expressions = Arrays.asList(expression1, expression2);

        Map<String, DoubleExpression> expressionMap = new HashMap<>();
        List<CalcResult> results1 = new ArrayList<>();
        for (DoubleExpression expression : expressions) {
            if (expression.codes().size()==1 && expression.codes().contains(expression.code()))
                results1.add(calcService.calc(expression));
            else
                expressionMap.putIfAbsent(expression.code(), expression);
        }


        List<CalcResult> results2;
        try {
            List<String> codes = expressionService.sort(expressionMap);

            List<CalcResult> list = new ArrayList<>();
            for (String c : codes) {
                DoubleExpression e = expressionMap.get(c);
                CalcResult result = calcService.calc(e);
                list.add(result);

                MeteringPoint meteringPoint = meteringPointRepo.findByCode(c);
                if (meteringPoint!=null) {
                    Parameter parameter = parameterRepo.findByCode("A+");
                    result.setMeteringDate(context.getEndDate().atStartOfDay().plusDays(1));
                    result.setMeteringPointId(meteringPoint.getId());
                    result.setParamId(parameter.getId());
                    result.setUnitId(parameter.getUnit().getId());
                    result.setParamType("AT");
                    context.getValues().add(result);
                }
            }
            results2 = list;

        }
        catch (Exception e) {
            e.printStackTrace();
            results2 = Collections.emptyList();
        }

        List<CalcResult> results = Stream.concat(results1.stream(), results2.stream()).collect(toList());
        results.stream().forEach(r -> System.out.println(r.getDoubleVal()));


        /*
        DoubleExpression expression1 = AtTimeValueExpression.builder()
            .context(context)
            .meteringPointCode("111111111111111111")
            .parameterCode("A+")
            .per("end")
            .rate(1d)
            .service(atTimeValueService)
            .build();

        DoubleExpression expression2 = AtTimeValueExpression.builder()
            .context(context)
            .meteringPointCode("111111111111111111")
            .parameterCode("A+")
            .per("start")
            .rate(1d)
            .service(atTimeValueService)
            .build();

        BinaryExpression expression3 = BinaryExpression.builder()
            .expressions(Arrays.asList(expression1, expression2))
            .operator(operatorFactory.binary("subtract"))
            .code("3333333333333333333")
            .build();

        BinaryExpression expression4 = BinaryExpression.builder()
            .expressions(Arrays.asList(expression1, expression2, expression3))
            .operator(operatorFactory.binary("add"))
            .code("444444444444444444")
            .build();
        */


        /*
        List<BalanceSubstResultHeader> headers = balanceSubstResultHeaderRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("Count of headers for calculations: " + headers.size());
        for (BalanceSubstResultHeader header : headers) {
            logger.info("Header " + header.getId() + " started");
            balanceSubstMrService.calc(header);
            balanceSubstUbService.calc(header);
            balanceSubstUService.calc(header);
            balanceSubstPeService.calc(header);
            logger.info("Header " + header.getId() + " completed");
        }
        */
    }
}
