package calc.formula.operand;

import calc.formula.expression.UnaryExpression;
import java.util.function.UnaryOperator;

public interface Operand {
    Double getValue();
    UnaryExpression andThen(UnaryOperator<Operand> operator);
}
