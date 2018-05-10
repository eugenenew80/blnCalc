package calc.formula.service;

import calc.formula.expression.DoubleExpression;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public interface OperatorFactory {
    BinaryOperator<DoubleExpression> binary(String operator);
    UnaryOperator<DoubleExpression> unary(String operator);
}
