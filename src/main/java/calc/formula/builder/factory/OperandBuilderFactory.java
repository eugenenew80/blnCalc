package calc.formula.builder.factory;

import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.builder.xml.OperandBuilder;

public interface OperandBuilderFactory {
    OperandBuilder getBuilder(String operandType, ExpressionBuilder expressionBuilder);
}
