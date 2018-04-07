package calc.formula.builder;

import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.service.XmlExpressionService;

public interface ExpressionBuilderFactory {
    ExpressionBuilder getBuilder(String expressionType, XmlExpressionService expressionService);
}
