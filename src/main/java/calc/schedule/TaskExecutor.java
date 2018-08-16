package calc.schedule;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.doc.impl.BalanceSubstMrServiceImpl;
import calc.formula.doc.impl.BalanceSubstPeServiceImpl;
import calc.formula.doc.impl.BalanceSubstUServiceImpl;
import calc.formula.doc.impl.BalanceSubstUbServiceImpl;
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
    private final BalanceSubstMrServiceImpl balanceSubstMrService;
    private final BalanceSubstUbServiceImpl balanceSubstUbService;
    private final BalanceSubstUServiceImpl balanceSubstUService;
    private final BalanceSubstPeServiceImpl balanceSubstPeService;

    @Scheduled(cron = "*/30 * * * * *")
    public void run() {
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
    }
}
