package calc.formula.service.impl;

import calc.formula.expression.Expression;
import calc.formula.expression.impl.DoubleValueExpression;
import calc.formula.service.OperatorFactory;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

@Service
public class OperatorFactoryImpl implements OperatorFactory {
    private Map<String, BinaryOperator<Expression>> binaryOperators = new HashMap<>();
    private Map<String, UnaryOperator<Expression>> unaryOperators = new HashMap<>();

    @Override
    public BinaryOperator<Expression> binary(String operator) {
        return binaryOperators.get(operator);
    }

    @Override
    public UnaryOperator<Expression> unary(String operator) {
        return unaryOperators.get(operator);
    }


    @PostConstruct
    private void init() {
        binaryOperators();
        unaryOperators();
    }

    private void binaryOperators() {
        BinaryOperator<Expression> add = (op1, op2) -> DoubleValueExpression.builder()
            .value(op1.value() + op2.value())
            .build();

        BinaryOperator<Expression> subtract = (op1, op2) -> DoubleValueExpression.builder()
            .value(op1.value() - op2.value())
            .build();

        BinaryOperator<Expression> divide = (op1, op2) -> DoubleValueExpression.builder()
            .value(op1.value() / op2.value())
            .build();

        BinaryOperator<Expression> multiply = (op1, op2) -> DoubleValueExpression.builder()
            .value(op1.value() * op2.value())
            .build();

        BinaryOperator<Expression> pow = (op1, op2) -> DoubleValueExpression.builder()
            .value(Math.pow(op1.value(), op2.value()))
            .build();

        BinaryOperator<Expression> max = (op1, op2) -> DoubleValueExpression.builder()
            .value(Math.max(op1.value(), op2.value()))
            .build();

        BinaryOperator<Expression> min = (op1, op2) -> DoubleValueExpression.builder()
            .value(Math.min(op1.value(), op2.value()))
            .build();

        binaryOperators.put("add", add);
        binaryOperators.put("subtract", subtract);
        binaryOperators.put("multiply", multiply);
        binaryOperators.put("divide", divide);
        binaryOperators.put("max", max);
        binaryOperators.put("min", min);
        binaryOperators.put("pow", pow);
    }

    private void unaryOperators() {
        UnaryOperator<Expression> nothing = (op) -> DoubleValueExpression.builder()
            .value(op.value())
            .build();

        UnaryOperator<Expression> minus = (op) -> DoubleValueExpression.builder()
            .value(-1*op.value())
            .build();

        UnaryOperator<Expression> abs = (op) -> DoubleValueExpression.builder()
            .value(Math.abs(op.value()))
            .build();

        UnaryOperator<Expression> ceil = (op) -> DoubleValueExpression.builder()
            .value(Math.ceil(op.value()))
            .build();

        UnaryOperator<Expression> floor = (op) -> DoubleValueExpression.builder()
            .value(Math.floor(op.value()))
            .build();

        UnaryOperator<Expression> sqrt = (op) -> DoubleValueExpression.builder()
            .value(Math.sqrt(op.value()))
            .build();

        UnaryOperator<Expression> pow2 = (op) -> DoubleValueExpression.builder()
            .value(Math.pow(op.value(),2))
            .build();

        UnaryOperator<Expression> sign = (op) -> DoubleValueExpression.builder()
            .value(Math.signum(op.value()))
            .build();

        UnaryOperator<Expression> round = (op) -> DoubleValueExpression.builder()
            .value(Math.round(op.value()*1d) / 1d)
            .build();

        UnaryOperator<Expression> round1 = (op) -> DoubleValueExpression.builder()
            .value(Math.round(op.value()*10d) / 10d)
            .build();

        UnaryOperator<Expression> round2 = (op) -> DoubleValueExpression.builder()
            .value(Math.round(op.value()*100d) / 100d)
            .build();

        UnaryOperator<Expression> round3 = (op) -> DoubleValueExpression.builder()
            .value(Math.round(op.value()*1000d) / 1000d)
            .build();

        UnaryOperator<Expression> round4 = (op) -> DoubleValueExpression.builder()
            .value(Math.round(op.value()*10000d) / 10000d)
            .build();

        unaryOperators.put("nothing", nothing);
        unaryOperators.put("minus", minus);
        unaryOperators.put("abs", abs);
        unaryOperators.put("ceil", ceil);
        unaryOperators.put("floor", floor);
        unaryOperators.put("sqrt", sqrt);
        unaryOperators.put("pow-2", pow2);
        unaryOperators.put("sign", sign);
        unaryOperators.put("round", round);
        unaryOperators.put("round-1", round1);
        unaryOperators.put("round-2", round2);
        unaryOperators.put("round-3", round3);
        unaryOperators.put("round-4", round4);
    }
}
