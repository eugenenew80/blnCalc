package calc.schedule;

import calc.entity.calc.Task;
import calc.entity.calc.TaskParam;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.repo.calc.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private final TaskRepo taskRepo;
    private final CalcService calcService;

    @Scheduled(cron = "0 */1 * * * *")
    public void run() {
        System.out.println("started");

        List<Task> tasks = taskRepo.findAll();
        tasks.stream()
            .filter(t -> t.getStatus().equals("W"))
            .flatMap(t -> t.getParams().stream())
            .map(t -> createContext(t))
            .forEach(context -> {
                try {
                    CalcResult result = calcService.calc("<pt code=\"123456789123456789\" param=\"A+\" />", context);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });

        System.out.println("completed");
    }


    private CalcContext createContext(TaskParam taskParam) {
        return CalcContext.builder()
            .startDate(taskParam.getStartDate())
            .endDate(taskParam.getEndDate())
            .orgId(taskParam.getOrg().getId())
            .energyObjectType("SUBST")
            .energyObjectId(taskParam.getSubstation().getId())
            .trace(new HashMap<>())
            .values(new ArrayList<>())
            .build();
    }

}
