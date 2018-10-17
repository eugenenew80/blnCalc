package calc.schedule;

import calc.entity.calc.asp.AspResultHeader;
import calc.entity.calc.bs.BalanceSubstResultHeaderW;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.svr.SvrHeader;
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
    private final BalanceSubstResultHeaderWRepo balanceSubstResultHeaderWRepo;
    private final BalanceSubstService balanceSubstService;
    private final AspResultHeaderRepo aspResultHeaderRepo;
    private final SvrHeaderRepo svrHeaderRepo;
    private final AspService aspService;
    private final SvrService svrService;

    @Scheduled(cron = "*/5 * * * * *")
    public void run() {
        calc_bs();
        calc_asp();
        calc_svr();
    }

    private void calc_bs() {
        List<BalanceSubstResultHeaderW> headers = balanceSubstResultHeaderWRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("BS calculation, count of tasks: " + headers.size());
        for (BalanceSubstResultHeaderW h : headers) {
            logger.info("Header " + h.getId() + " started");
            balanceSubstService.calc(h.getId());
            logger.info("Header " + h.getId() + " completed");
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
