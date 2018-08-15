package calc.schedule;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.doc.impl.BalanceSubstMrServiceImpl;
import calc.formula.doc.impl.BalanceSubstUServiceImpl;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstMrServiceImpl balanceSubstMrService;
    private final BalanceSubstUServiceImpl balanceSubstUService;

    @Scheduled(cron = "0 */1 * * * *")
    public void run() {
        System.out.println("started");

        List<BalanceSubstResultHeader> headers = balanceSubstResultHeaderRepo.findAll();
        for (BalanceSubstResultHeader header : headers) {
            if (header.getStatus() != BatchStatusEnum.W) continue;
            balanceSubstMrService.calc(header);
            balanceSubstUService.calc(header);
        }

        System.out.println("completed");
    }
}
