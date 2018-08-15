package calc.schedule;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.FormulaTypeEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private final TaskRepo taskRepo;
    private final CalcService calcService;
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final ParameterRepo parameterRepo;
    private final UnitRepo unitRepo;

    @Scheduled(cron = "0 */1 * * * *")
    public void run() {
        System.out.println("started");

        Parameter parAp = parameterRepo.findByCode("A+");
        Parameter parAm = parameterRepo.findByCode("A-");
        Parameter parRp = parameterRepo.findByCode("R+");
        Parameter parRm = parameterRepo.findByCode("R-");
        Unit aUnitCode = unitRepo.findByCode("kW.h");

        List<BalanceSubstResultHeader> headers = balanceSubstResultHeaderRepo.findAll();
        for (BalanceSubstResultHeader header : headers) {
            if (header.getStatus() != BatchStatusEnum.W)
                continue;

            for (BalanceSubstResultMrLine mrLine : header.getMrLines())
                balanceSubstResultMrLineRepo.delete(mrLine);

            updateStatus(header, BatchStatusEnum.P);
            try {
                CalcContext context = CalcContext.builder()
                    .startDate(header.getStartDate())
                    .endDate(header.getEndDate())
                    .orgId(header.getOrganization().getId())
                    .energyObjectType("SUBSTATION")
                    .energyObjectId(header.getSubstation().getId())
                    .docCode("ACT")
                    .docId(header.getId())
                    .trace(new HashMap<>())
                    .values(new ArrayList<>())
                    .build();

                List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
                List<BalanceSubstMrLine> mrLines = header.getHeader().getMrLines();
                for (BalanceSubstMrLine mrLine : mrLines) {
                    List<MeterHistory> meters = mrLine.getMeteringPoint().getMeters();
                    for (Parameter param : Arrays.asList(parAp, parAm, parRp, parRm)) {
                        Map<String, Formula> formulas = createFormulas(mrLine, param);
                        BalanceSubstResultMrLine line = calcMrLines(formulas, context);

                        if (mrLine.getIsSection1()) line.setSection("1");
                        if (mrLine.getIsSection2()) line.setSection("2");
                        if (mrLine.getIsSection3()) line.setSection("3");
                        if (mrLine.getIsSection4()) line.setSection("4");
                        if (mrLine.getIsSection5()) line.setSection("5");

                        line.setHeader(header);
                        line.setMeteringPoint(mrLine.getMeteringPoint());
                        line.setParam(param);
                        line.setUnit(aUnitCode);
                        line.setStartMeteringDate(context.getStartDate().atStartOfDay());
                        line.setEndMeteringDate(context.getEndDate().atStartOfDay().plusDays(1));

                        if (line.getStartVal() == null && line.getEndVal()==null)
                            line.setDelta(null);

                        if (meters.size()>0) {
                            line.setMeter(meters.get(0).getMeter());
                            line.setMeterRate(meters.get(0).getFactor());
                            if (line.getMeterRate()!=null && line.getDelta()!=null)
                                line.setVal(line.getDelta() * line.getMeterRate());
                        }

                        line.setIsIgnore(false);
                        line.setIsBypassSection(false);
                        line.setBypassMeteringPoint(null);

                        resultLines.add(line);
                    }
                }

                balanceSubstResultMrLineRepo.save(resultLines);
                updateStatus(header, BatchStatusEnum.C);
            }

            catch (Exception e) {
                updateStatus(header, BatchStatusEnum.E);
                e.printStackTrace();
            }
        }

        System.out.println("completed");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }


    private BalanceSubstResultMrLine calcMrLines(Map<String, Formula> formulas, CalcContext context) {
        BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();

        for (String key : formulas.keySet()) {
            Formula formula = formulas.get(key);
            String formulaText = formulaToString(formula);
            try {
                CalcResult result = calcService.calc(formulaText, context);
                if (key.equals("start-val"))
                    line.setStartVal(result.getDoubleVal());

                if (key.equals("end-val"))
                    line.setEndVal(result.getDoubleVal());

                if (key.equals("delta"))
                    line.setDelta(result.getDoubleVal());
            }
            catch (Exception e) {
                System.out.println(formulaText);
                e.printStackTrace();
            }
        }

        return line;
    }

    private Map<String, Formula> createFormulas(BalanceSubstMrLine mrLine, Parameter parameter) {
        Map<String, Formula> map = new HashMap<>();
        map.putIfAbsent("start-val", createFormula(mrLine, parameter, ParamTypeEnum.ATS));
        map.putIfAbsent("end-val", createFormula(mrLine, parameter, ParamTypeEnum.ATE));
        map.putIfAbsent("delta", createFormula(mrLine, parameter, ParamTypeEnum.DELTA));
        return map;
    }

    private Formula createFormula(BalanceSubstMrLine mrLine, Parameter parameter, ParamTypeEnum paramType) {
        Formula formula = new Formula();
        formula.setText("a0");
        formula.setMeteringPoint(mrLine.getMeteringPoint());
        formula.setFormulaType(FormulaTypeEnum.DIALOG);
        formula.setVars(new ArrayList<>());

        FormulaVar var = new FormulaVar();
        formula.getVars().add(var);

        var.setFormula(formula);
        var.setVarName("a0");
        var.setDetails(new ArrayList<>());

        FormulaVarDet det = new FormulaVarDet();
        var.getDetails().add(det);
        if (paramType == ParamTypeEnum.DELTA) {
            det.setFormula(formula);
            det.setFormulaVar(var);
            det.setMeteringPoint(mrLine.getMeteringPoint());
            det.setParamType(ParamTypeEnum.ATE);
            det.setRate(1d);
            det.setSign("+");
            det.setParam(parameter);

            det = new FormulaVarDet();
            var.getDetails().add(det);

            det.setFormula(formula);
            det.setFormulaVar(var);
            det.setMeteringPoint(mrLine.getMeteringPoint());
            det.setParamType(ParamTypeEnum.ATS);
            det.setRate(1d);
            det.setSign("-");
            det.setParam(parameter);
        }
        else {
            det.setFormula(formula);
            det.setFormulaVar(var);
            det.setMeteringPoint(mrLine.getMeteringPoint());
            det.setParamType(paramType);
            det.setRate(1d);
            det.setSign("+");
            det.setParam(parameter);
        }

        return formula;
    }

    private String formulaToString(Formula formula) {
        if (formula.getFormulaType() != FormulaTypeEnum.DIALOG)
            return "";

        String params = "<params>";
        for (FormulaVar var : formula.getVars())
            params = params + varToString(var);
        params = params + "</params>";

        return  "<js><src>" + formula.getText() + "</src>" + params + "</js>";
    }

    private String varToString(FormulaVar var) {
        String formulaVar =  "<param name=\"" + var.getVarName() + "\">" + "<add>";
        for (FormulaVarDet det : var.getDetails()) {
            String formulaDet = detToString(det);
            formulaVar = formulaVar + formulaDet;
        }
        return formulaVar + "</add>" + "</param>";
    }

    private String detToString(FormulaVarDet det) {
        String mp = det.getMeteringPoint().getCode();
        String param = det.getParam().getCode();
        String per = "end";

        String paramType = "pt";
        if (det.getParamType() == ParamTypeEnum.PT )
            paramType = "pt";

        if (det.getParamType() == ParamTypeEnum.AT || det.getParamType() == ParamTypeEnum.ATS || det.getParamType() == ParamTypeEnum.ATE)
            paramType = "at";

        if (det.getParamType() == ParamTypeEnum.ATS)
            per = "start";

        if (det.getParamType() == ParamTypeEnum.ATE)
            per = "end";

        String formula = "<" + paramType + " mp=\"" + mp + "\" param=\"" + param + "\" per=\"" + per + "\" />";

        if (det.getSign()!=null && det.getSign().equals("-"))
            formula = "<minus>" + formula + "</minus>";

        if (det.getRate()!=null && det.getRate()!=1d)
            formula = "<multiply>" + "<number val=\"" + det.getRate().toString() + "\" />"  + formula + "</multiply>";

        return formula;
    }
}
