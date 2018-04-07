package calc.formula.builder.factory;

import calc.formula.builder.xml.*;
import calc.formula.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.script.ScriptEngine;

@Service
@RequiredArgsConstructor
public class OperandBuilderFactoryImp implements OperandBuilderFactory {
    private final AtTimeValueService atTimeValueService;
    private final PeriodTimeValueService periodTimeValueService;
    private final PowerLineService powerLineService;
    private final PowerTransformerService powerTransformerService;
    private final ReactorService reactorService;
    private final ScriptEngine engine;

    public OperandBuilder getBuilder(String operandType, ExpressionBuilder expressionBuilder) {
        if (operandType.equals("pt"))
            return new PeriodTimeValueOperandBuilder(periodTimeValueService);

        if (operandType.equals("at"))
            return new AtTimeValueOperandBuilder(atTimeValueService);

        if (operandType.equals("js"))
            return new JavaScriptOperandBuilder(engine, expressionBuilder);

        if (operandType.equals("li"))
            return new PowerLineOperandBuilder(powerLineService);

        if (operandType.equals("tr"))
            return new PowerTransformerOperandBuilder(powerTransformerService);

        if (operandType.equals("re"))
            return new ReactorOperandBuilder(reactorService);

        if (operandType.equals("number"))
            return new DoubleValueOperandBuilder();

        return null;
    }
}
