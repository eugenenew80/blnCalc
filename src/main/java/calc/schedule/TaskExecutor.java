package calc.schedule;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Task;
import calc.entity.calc.TaskParam;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.expression.DoubleExpression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.calc.MeteringPointRepo;
import calc.repo.calc.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private final TaskRepo taskRepo;
    private final CalcService calcService;
    private final ExpressionService expressionService;
    private final MeteringPointRepo meteringPointRepo;

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
                    Map<String, DoubleExpression> expressionMap = new HashMap<>();

                    String mpCode = "123456789123456789";
                    String param = "A+";
                    MeteringPoint mp = meteringPointRepo.findByCode(mpCode);

                    Formula formula = new Formula();
                    formula.setCode(mpCode);
                    formula.setText("<pt mpCode=\"" + mpCode + "\" param=\"" + param + "\" />");
                    formula.setDescription("Formula");
                    formula.setMeteringPoint(mp);

                    DoubleExpression expr = expressionService.parse(formula, context);
                    expressionMap.putIfAbsent(mpCode, expr);

                    List<String> mpCodes = expressionService.sort(expressionMap);
                    for (String code : mpCodes) {

                    }

                    CalcResult result = calcService.calc("<pt mpCode=\"123456789123456789\" param=\"A+\" />", context);
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
            .docCode(taskParam.getTask().getDocCode())
            .docId(taskParam.getTask().getId())
            .trace(new HashMap<>())
            .values(new ArrayList<>())
            .build();
    }

}
