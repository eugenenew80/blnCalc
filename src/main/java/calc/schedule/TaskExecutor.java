package calc.schedule;

import calc.entity.calc.Task;
import calc.repo.calc.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private final TaskRepo taskRepo;

    @Scheduled(cron = "0 */1 * * * *")
    public void run() {
        System.out.println("started");
        List<Task> tasks = taskRepo.findAll();
        System.out.println(tasks.size());

        System.out.println(tasks.get(0).getParams().size());

        System.out.println("completed");
    }



}
