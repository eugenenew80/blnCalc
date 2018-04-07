package calc.formula.expression;

import calc.formula.operand.Operand;

public interface Expression extends Operand {
    Operand calc();
}
