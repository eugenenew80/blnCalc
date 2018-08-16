package calc.formula.doc.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.FormulaTypeEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceSubstUServiceImpl {
    private final CalcService calcService;
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;
    private final ParameterRepo parameterRepo;

    public void calc(BalanceSubstResultHeader header)  {
        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            header = balanceSubstResultHeaderRepo.findOne(header.getId());

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

            Parameter parU = parameterRepo.findByCode("U");

            List<BalanceSubstResultULine> resultLines = new ArrayList<>();
            List<BalanceSubstULine> uLines = header.getHeader().getULines();
            for (BalanceSubstULine uLine : uLines) {
                Formula formula = createFormula(uLine.getMeteringPoint(), parU, ParamTypeEnum.PT);
                BalanceSubstResultULine resultLine = calcLine(formula, context);
                resultLine.setHeader(header);
                resultLine.setMeteringPoint(uLine.getMeteringPoint());
                resultLine.setTiNum(uLine.getTiNum());
                resultLine.setTiName(uLine.getTiName());
                resultLines.add(resultLine);
            }

            balanceSubstResultULineRepo.save(resultLines);
            updateStatus(header, BatchStatusEnum.C);
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            e.printStackTrace();
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultULine> lines = balanceSubstResultULineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultULineRepo.delete(lines.get(i));
        balanceSubstResultULineRepo.flush();

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }

    private BalanceSubstResultULine calcLine(Formula formula, CalcContext context) {
        BalanceSubstResultULine line = new BalanceSubstResultULine();
        String formulaText = calcService.formulaToString(formula);
        System.out.println(formulaText);
        try {
            CalcResult result = calcService.calc(formulaText, context);

            Double sum = 0d;
            Double count = 0d;
            for (Double d : result.getDoubleValues()) {
                if (d!=null) {
                    sum+=d;
                    count++;
                }
            }
            line.setVal(sum / count);
        }
        catch (Exception e) {
            System.out.println(formulaText);
            e.printStackTrace();
        }
        return line;
    }

    private Formula createFormula(MeteringPoint meteringPoint, Parameter parameter, ParamTypeEnum paramType) {
        Formula formula = new Formula();
        formula.setText("a0");
        formula.setMeteringPoint(meteringPoint);
        formula.setFormulaType(FormulaTypeEnum.DIALOG);
        formula.setVars(new ArrayList<>());

        FormulaVar var = new FormulaVar();
        formula.getVars().add(var);

        var.setFormula(formula);
        var.setVarName("a0");
        var.setDetails(new ArrayList<>());

        FormulaVarDet det = new FormulaVarDet();
        var.getDetails().add(det);

        det.setFormula(formula);
        det.setFormulaVar(var);
        det.setMeteringPoint(meteringPoint);
        det.setParamType(paramType);
        det.setRate(1d);
        det.setSign("+");
        det.setParam(parameter);

        return formula;
    }
}
