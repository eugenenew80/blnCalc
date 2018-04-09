package calc.formula.expression;

import calc.formula.expression.impl.UnaryExpression;

import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public interface Expression {
    Expression expression();
    Double value();

    default Set<String> meteringPoints() {
        return Collections.emptySet();
    }

    default UnaryExpression andThen(UnaryOperator<Expression> operator) {
        return UnaryExpression.builder()
            .expression(this)
            .operator(operator)
            .build();
    }
}
