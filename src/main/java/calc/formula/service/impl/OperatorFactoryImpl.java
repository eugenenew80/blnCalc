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

    private BinaryOperator<Expression> arrOperator(BinaryOperator<Expression> operator) {
        return  (op1, op2) -> {
            Double[] values1 = op1.values();
            Double[] values2 = op2.values();

            int resultLength = Math.min(values1.length, values2.length);
            Double[] results = new Double[resultLength];
            for (int i=0; i<resultLength; i++) {
                results[i] = operator.apply(
                    DoubleValueExpression.builder().value(values1[i]).build(),
                    DoubleValueExpression.builder().value(values2[i]).build()
                ).value();
            }

            Double result = operator.apply(op1, op2).value();

            DoubleValueExpression expression = DoubleValueExpression.builder()
                .value(result)
                .values(results)
                .build();

            return expression;
        };
    }

    private void binaryOperators() {
        BinaryOperator<Expression> add = (op1, op2) -> {
            Double v1 = op1.value();
            Double v2 = op2.value();
            Double res = null;
            if (v1!=null || v2!=null) {
                if (v1==null) v1 = 0d;
                if (v2==null) v2 = 0d;
                res = v1 + v2;
            }
            return DoubleValueExpression.builder()
                    .value(res)
                    .build();
        };
        add = arrOperator(add);

        BinaryOperator<Expression> subtract = (op1, op2) -> {
            Double v1 = op1.value();
            Double v2 = op2.value();
            Double res = null;
            if (v1!=null || v2!=null) {
                if (v1==null) v1 = 0d;
                if (v2==null) v2 = 0d;
                res = v1 - v2;
            }
            return DoubleValueExpression.builder()
                    .value(res)
                    .build();
        };
        subtract = arrOperator(subtract);

        BinaryOperator<Expression> divide = (op1, op2) -> {
            Double v1 = op1.value();
            Double v2 = op2.value();
            Double res = null;
            if (v1!=null || v2!=null) {
                if (v1==null) v1 = 0d;
                if (v2==null) v2 = 0d;
                res = v1 / v2;
            }
            return DoubleValueExpression.builder()
                    .value(res)
                    .build();
        };
        divide = arrOperator(divide);

        BinaryOperator<Expression> multiply = (op1, op2) -> {
            Double v1 = op1.value();
            Double v2 = op2.value();
            Double res = null;
            if (v1!=null || v2!=null) {
                if (v1==null) v1 = 0d;
                if (v2==null) v2 = 0d;
                res = v1 * v2;
            }
            return DoubleValueExpression.builder()
                    .value(res)
                    .build();
        };
        multiply = arrOperator(multiply);

        BinaryOperator<Expression> pow = (op1, op2) -> {
            Double v1 = op1.value();
            Double v2 = op2.value();
            Double res = null;
            if (v1!=null || v2!=null) {
                if (v1==null) v1 = 0d;
                if (v2==null) v2 = 0d;
                res = Math.pow(v1, v2);
            }
            return DoubleValueExpression.builder()
                    .value(res)
                    .build();
        };
        pow = arrOperator(pow);

        BinaryOperator<Expression> max = (op1, op2) -> {
            Double v1 = op1.value();
            Double v2 = op2.value();
            Double res = null;
            if (v1!=null || v2!=null) {
                if (v1==null) v1 = Double.MIN_VALUE;
                if (v2==null) v2 = Double.MIN_VALUE;
                res = Math.max(v1, v2);
            }
            return DoubleValueExpression.builder()
                .value(res)
                .build();
        };
        max = arrOperator(max);

        BinaryOperator<Expression> min = (op1, op2) ->  {
            Double v1 = op1.value();
            Double v2 = op2.value();
            Double res = null;
            if (v1!=null || v2!=null) {
                if (v1==null) v1 = Double.MAX_VALUE;
                if (v2==null) v2 = Double.MAX_VALUE;
                res = Math.min(v1, v2);
            }
            return DoubleValueExpression.builder()
                .value(res)
                .build();
        };
        min = arrOperator(min);

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
