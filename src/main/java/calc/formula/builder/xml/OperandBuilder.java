package calc.formula.builder.xml;

import calc.formula.CalcContext;
import calc.formula.operand.Operand;
import org.w3c.dom.Node;

public interface OperandBuilder<T extends Operand> {
    T build(Node node, CalcContext context);
}
