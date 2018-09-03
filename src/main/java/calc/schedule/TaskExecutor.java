package calc.schedule;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.calculation.BalanceSubstMrService;
import calc.formula.calculation.BalanceSubstPeService;
import calc.formula.calculation.BalanceSubstUService;
import calc.formula.calculation.BalanceSubstUbService;
import calc.formula.exception.CycleDetectionException;
import calc.formula.service.CalcService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstMrService balanceSubstMrService;
    private final BalanceSubstUbService balanceSubstUbService;
    private final BalanceSubstUService balanceSubstUService;
    private final BalanceSubstPeService balanceSubstPeService;
    private final CalcService calcService;
    private final MeteringPointRepo meteringPointRepo;


    @Scheduled(cron = "*/30 * * * * *")
    public void run() {
        //test();

        List<BalanceSubstResultHeader> headers = balanceSubstResultHeaderRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("Count of headers for calculation: " + headers.size());
        for (BalanceSubstResultHeader header : headers) {
            logger.info("Header " + header.getId() + " started");
            //balanceSubstMrService.calc(header);
            //balanceSubstUbService.calc(header);
            //balanceSubstUService.calc(header);
            balanceSubstPeService.calc(header);
            logger.info("Header " + header.getId() + " completed");
        }
    }

    private void test() {
        CalcContext context = CalcContext.builder()
            .headerId(1l)
            .periodType(PeriodTypeEnum.D)
            .startDate(LocalDate.of(2018, Month.AUGUST, 13))
            .endDate(LocalDate.of(2018, Month.AUGUST, 13))
            .orgId(1l)
            .energyObjectType("SUBSTATION")
            .energyObjectId(1l)
            .isMeteringReading(true)
            .trace(new HashMap<>())
            .values(new HashMap<>())
            .build();

        MeteringPoint meteringPoint = meteringPointRepo.findOne(5l);
        try {
            List<CalcResult> results = calcService.calcMeteringPoints(Arrays.asList(meteringPoint), "A+", context);
            //Double value = results.size() > 0 ? results.get(0).getDoubleValue() : null;
            //System.out.println(value);

            System.out.println(results.size());
        }
        catch (CycleDetectionException e) {
            logger.error("Циклическая формула для точки учёта: " + e.getCode());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
