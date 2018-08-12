package calc.schedule;

import calc.entity.calc.*;
import calc.formula.CalcResult;
import calc.formula.doc.impl.BalanceSubstServiceImpl;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.calc.BalanceSubstHeaderRepo;
import calc.repo.calc.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private final TaskRepo taskRepo;
    private final BalanceSubstServiceImpl balanceSubstService;

    @Scheduled(cron = "0 */1 * * * *")
    public void run() {
        System.out.println("started");

        List<Task> tasks = taskRepo.findAll();
        tasks.stream()
            .filter(t -> t.getStatus().equals("W"))
            .flatMap(t -> t.getParams().stream())
            .forEach(t -> {
                try {
                    Map<String, List<CalcResult>> results = balanceSubstService.calc(t);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });

        System.out.println("completed");
    }
}
