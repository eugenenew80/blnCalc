package kz.kegoc.bln.calc.operand;

import kz.kegoc.bln.calc.expression.UnaryExpression;
import java.util.function.UnaryOperator;

public interface Operand {
    Double getValue();
    UnaryExpression andThen(UnaryOperator<Operand> operator);
}
