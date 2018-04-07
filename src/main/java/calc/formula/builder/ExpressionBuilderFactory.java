package calc.formula.builder;

import calc.formula.builder.impl.RootExpressionBuilder;

public interface ExpressionBuilderFactory {
    ExpressionBuilder getBuilder(String operandType, RootExpressionBuilder expressionBuilder);
}
