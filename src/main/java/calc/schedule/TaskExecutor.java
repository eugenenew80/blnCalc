package calc.schedule;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.calculation.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
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
    private final BalanceSubstPeService balanceSubstService;
    private final AspResultHeaderRepo aspResultHeaderRepo;
    private final SvrHeaderRepo svrHeaderRepo;
    private final AspService aspService;
    private final SvrService svrService;


    @Scheduled(cron = "*/30 * * * * *")
    public void run() {
        calc_bs();
        calc_asp();
        calc_svr();
    }

    private void calc_bs() {
        List<BalanceSubstResultHeader> headers = balanceSubstResultHeaderRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("Balance calculation, count of tasks: " + headers.size());
        for (BalanceSubstResultHeader header : headers) {
            logger.info("Header " + header.getId() + " started");
            if (!balanceSubstMrService.calc(header))
                continue;

            if (!balanceSubstUbService.calc(header))
                continue;

            if (!balanceSubstUService.calc(header))
                continue;

            if (!balanceSubstPeService.calc(header))
                continue;

            if (!balanceSubstService.calc(header))
                continue;

            logger.info("Header " + header.getId() + " completed");
        }
    }

    private void calc_asp() {
        List<AspResultHeader> headers = aspResultHeaderRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("ASP calculation, count of tasks: " + headers.size());
        for (AspResultHeader header : headers) {
            logger.info("Header " + header.getId() + " started");
            aspService.calc(header);
            logger.info("Header " + header.getId() + " completed");
        }
    }

    private void calc_svr() {
        List<SvrHeader> headers = svrHeaderRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("SVR calculation, count of tasks: " + headers.size());
        for (SvrHeader header : headers) {
            logger.info("Header " + header.getId() + " started");
            svrService.calc(header);
            logger.info("Header " + header.getId() + " completed");
        }
    }
}
