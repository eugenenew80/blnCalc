package calc.formula.service.impl;

import calc.entity.calc.*;
import calc.entity.calc.enums.FormulaTypeEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcResult;
import calc.formula.CalcContext;
import calc.formula.expression.DoubleExpression;
import calc.formula.expression.StringExpression;
import calc.formula.service.CalcService;
import calc.formula.service.ExpressionService;
import calc.repo.calc.FormulaRepo;
import calc.repo.calc.MeteringPointRepo;
import calc.repo.calc.ParameterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalcServiceImpl implements CalcService {
    private final ExpressionService expressionService;
    private final FormulaRepo formulaRepo;
    private final MeteringPointRepo meteringPointRepo;
    private final ParameterRepo parameterRepo;

    @Override
    public CalcResult calc(String text, CalcContext context) throws Exception {
        Formula formula = new Formula();
        formula.setText(text);

        DoubleExpression expression = expressionService.parse(formula, context);
        return calc(expression);
    }

    @Override
    public CalcResult calc(DoubleExpression expression) {
        CalcResult result = new CalcResult();
        result.setDoubleVal(expression.doubleValue());
        result.setDoubleValues(expression.doubleValues());

        if (expression instanceof StringExpression) {
            StringExpression stringExpression = (StringExpression) expression;
            result.setStringVal(stringExpression.stringValue());
        }

        return result;
    }

    public String formulaToString(Formula formula) {
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


    public Formula createFormula(MeteringPoint meteringPoint, Parameter parameter, ParamTypeEnum paramType) {
        Formula formula = new Formula();
        formula.setText("a0");
        formula.setMeteringPoint(meteringPoint);
        formula.setParam(parameter);
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
            det.setMeteringPoint(meteringPoint);
            det.setParamType(ParamTypeEnum.ATE);
            det.setRate(1d);
            det.setSign("+");
            det.setParam(parameter);

            det = new FormulaVarDet();
            var.getDetails().add(det);

            det.setFormula(formula);
            det.setFormulaVar(var);
            det.setMeteringPoint(meteringPoint);
            det.setParamType(ParamTypeEnum.ATS);
            det.setRate(1d);
            det.setSign("-");
            det.setParam(parameter);
        }
        else {
            det.setFormula(formula);
            det.setFormulaVar(var);
            det.setMeteringPoint(meteringPoint);
            det.setParamType(paramType);
            det.setRate(1d);
            det.setSign("+");
            det.setParam(parameter);
        }

        return formula;
    }
}
