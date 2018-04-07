package calc.formula.expression;

import calc.formula.expression.impl.UnaryExpression;
import java.util.function.UnaryOperator;

public interface Expression {
    Expression calc();
    Double getValue();

    default UnaryExpression andThen(UnaryOperator<Expression> operator) {
        return UnaryExpression.builder()
            .expression(this)
            .operator(operator)
            .build();
    }
}
