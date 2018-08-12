package calc.formula.expression;

import calc.entity.calc.Formula;
import calc.formula.expression.impl.UnaryExpression;
import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public interface DoubleExpression {
    DoubleExpression doubleExpression();
    Double doubleValue();

    default Double[] doubleValues() {
        Double v = doubleValue();
        return new Double[] {v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v, v};
    }

    default Set<String> meteringPoints() {
        return Collections.emptySet();
    }

    default UnaryExpression andThen(UnaryOperator<DoubleExpression> operator) {
        return UnaryExpression.builder()
            .expression(this)
            .operator(operator)
            .build();
    }

    Formula getFormula();
}
