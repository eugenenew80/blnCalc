package calc.formula.builder.impl;

import calc.formula.builder.ExpressionBuilder;
import calc.formula.builder.ExpressionBuilderFactory;
import calc.formula.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.script.ScriptEngine;

@Service
@RequiredArgsConstructor
public class DefaultExpressionBuilderFactory implements ExpressionBuilderFactory {
    private final AtTimeValueService atTimeValueService;
    private final PeriodTimeValueService periodTimeValueService;
    private final PowerLineService powerLineService;
    private final PowerTransformerService powerTransformerService;
    private final ReactorService reactorService;
    private final ScriptEngine engine;

    public ExpressionBuilder getBuilder(String operandType, RootExpressionBuilder expressionBuilder) {
        if (operandType.equals("pt"))
            return new PeriodTimeValueExpressionBuilder(periodTimeValueService);

        if (operandType.equals("at"))
            return new AtTimeValueExpressionBuilder(atTimeValueService);

        if (operandType.equals("js"))
            return new JavaScriptExpressionBuilder(engine, expressionBuilder);

        if (operandType.equals("li"))
            return new PowerLineExpressionBuilder(powerLineService);

        if (operandType.equals("tr"))
            return new PowerTransformerExpressionBuilder(powerTransformerService);

        if (operandType.equals("re"))
            return new ReactorExpressionBuilder(reactorService);

        if (operandType.equals("number"))
            return new DoubleValueExpressionBuilder();

        return null;
    }
}
