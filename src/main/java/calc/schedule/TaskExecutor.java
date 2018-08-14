package calc.schedule;

import calc.entity.calc.*;
import calc.entity.calc.enums.FormulaTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.doc.impl.BalanceSubstServiceImpl;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.calc.BalanceSubstHeaderRepo;
import calc.repo.calc.FormulaRepo;
import calc.repo.calc.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private final TaskRepo taskRepo;
    private final BalanceSubstServiceImpl balanceSubstService;
    private final FormulaRepo formulaRepo;
    private final CalcService calcService;

    @Scheduled(cron = "0 */1 * * * *")
    public void run() {
        System.out.println("started");

        TaskParam taskParam = taskRepo.findAll().get(0).getParams().get(0);

        List<Formula> formulas = formulaRepo.findAll();
        for (Formula formula : formulas) {
            if (formula.getFormulaType() == FormulaTypeEnum.DIALOG) {
                for (FormulaVar var : formula.getVars()) {
                    String formulaVar = "<params>";
                    formulaVar = formulaVar + "<param name=\"" + var.getVarName() + "\">";

                    formulaVar = formulaVar + "<add>";
                    for (FormulaVarDet det : var.getDetails()) {
                        String formulaDet = detToStrimg(det);
                        formulaVar = formulaVar + formulaDet;
                    }
                    formulaVar = formulaVar + "</add>";

                    formulaVar = formulaVar + "</param>";
                    formulaVar = formulaVar + "</params>";

                    System.out.println(formulaVar);
                    /*
                    try {
                        CalcContext context = createContext(taskParam);
                        CalcResult calcResult = calcService.calc(formulaVar, context);
                        System.out.println(Arrays.deepToString(calcResult.getDoubleValues()));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    */
                }
            }
        }




        /*
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
        */

        System.out.println("completed");
    }


    private String detToStrimg(FormulaVarDet det) {
        String mp = det.getMeteringPoint().getCode();
        String param = det.getParam().getCode();
        String formula = "<pt mp=\"" + mp + "\" param=\"" + param + "\" />";

        if (det.getSign()!=null && det.getSign().equals("-"))
            formula = "<minus>" + formula + "</minus>";

        if (det.getRate()!=null && det.getRate()!=1d)
            formula = "<multiply>" + "<number val=\"" + det.getRate().toString() + "\" />"  + formula + "</multiply>";

        return formula;
    }

    private CalcContext createContext(TaskParam taskParam) {
        return CalcContext.builder()
            .startDate(taskParam.getStartDate())
            .endDate(taskParam.getEndDate())
            .orgId(taskParam.getOrg().getId())
            .energyObjectType("SUBSTATION")
            .energyObjectId(taskParam.getSubstation().getId())
            .docCode(taskParam.getTask().getDocCode())
            .docId(taskParam.getTask().getId())
            .trace(new HashMap<>())
            .values(new ArrayList<>())
            .build();
    }
}
