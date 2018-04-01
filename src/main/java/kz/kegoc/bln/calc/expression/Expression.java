package kz.kegoc.bln.calc.expression;

import kz.kegoc.bln.calc.operand.Operand;

public interface Expression extends Operand {
    Operand calc();
}
