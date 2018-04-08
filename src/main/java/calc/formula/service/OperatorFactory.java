package calc.formula.service;

import calc.formula.expression.Expression;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public interface OperatorFactory {
    BinaryOperator<Expression> binary(String operator);
    UnaryOperator<Expression> unary(String operator);
}
