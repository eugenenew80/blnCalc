package calc.formula.builder.xml.impl;

import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.builder.ExpressionBuilderFactory;
import calc.formula.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.script.ScriptEngine;

@Service
@RequiredArgsConstructor
public class XmlExpressionBuilderFactory implements ExpressionBuilderFactory {
    private final AtTimeValueService atTimeValueService;
    private final PeriodTimeValueService periodTimeValueService;
    private final PowerLineService powerLineService;
    private final PowerTransformerService powerTransformerService;
    private final ReactorService reactorService;
    private final ScriptEngine engine;

    public ExpressionBuilder getBuilder(String expressionType, XmlExpressionService expressionService) {
        if (expressionType.equals("pt"))
            return new PeriodTimeValueExpressionBuilder(periodTimeValueService);

        if (expressionType.equals("at"))
            return new AtTimeValueExpressionBuilder(atTimeValueService);

        if (expressionType.equals("js"))
            return new JsExpressionBuilder(engine, expressionService);

        if (expressionType.equals("li"))
            return new PowerLineExpressionBuilder(powerLineService);

        if (expressionType.equals("tr"))
            return new PowerTransformerExpressionBuilder(powerTransformerService);

        if (expressionType.equals("re"))
            return new ReactorExpressionBuilder(reactorService);

        if (expressionType.equals("number"))
            return new DoubleValueExpressionBuilder();

        return null;
    }
}
