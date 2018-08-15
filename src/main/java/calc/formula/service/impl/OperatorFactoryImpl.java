package calc.formula.service.impl;

import calc.formula.expression.DoubleExpression;
import calc.formula.expression.impl.DoubleValueExpression;
import calc.formula.service.OperatorFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

@Service
public class OperatorFactoryImpl implements OperatorFactory {
    private Map<String, BinaryOperator<DoubleExpression>> binaryOperators = new HashMap<>();
    private Map<String, UnaryOperator<DoubleExpression>> unaryOperators = new HashMap<>();

    @Override
    public BinaryOperator<DoubleExpression> binary(String operator) {
        return binaryOperators.get(operator);
    }

    @Override
    public UnaryOperator<DoubleExpression> unary(String operator) {
        return unaryOperators.get(operator);
    }


    @PostConstruct
    private void init() {
        binaryOperators();
        unaryOperators();
    }

    private void binaryOperators() {
        BinaryOperator<DoubleExpression> add = (op1, op2) -> {
            Pair<Double, Double> doublePair = validate(op1, op2, 0d);
            return DoubleValueExpression.builder()
                .value(doublePair.getLeft() + doublePair.getRight())
                .build();
        };
        add = arrOperator(add);

        BinaryOperator<DoubleExpression> subtract = (op1, op2) -> {
            Pair<Double, Double> doublePair = validate(op1, op2, 0d);
            return DoubleValueExpression.builder()
                .value(doublePair.getLeft() - doublePair.getRight())
                .build();
        };
        subtract = arrOperator(subtract);

        BinaryOperator<DoubleExpression> divide = (op1, op2) -> {
            Pair<Double, Double> doublePair = validate(op1, op2, 0d);
            return DoubleValueExpression.builder()
                .value(doublePair.getLeft() / doublePair.getRight())
                .build();
        };
        divide = arrOperator(divide);

        BinaryOperator<DoubleExpression> multiply = (op1, op2) -> {
            Pair<Double, Double> doublePair = validate(op1, op2, 0d);
            return DoubleValueExpression.builder()
                .value(doublePair.getLeft() * doublePair.getRight())
                .build();
        };
        multiply = arrOperator(multiply);

        BinaryOperator<DoubleExpression> pow = (op1, op2) -> {
            Pair<Double, Double> doublePair = validate(op1, op2, 0d);
            return DoubleValueExpression.builder()
                .value(Math.pow(doublePair.getLeft(), doublePair.getRight()))
                .build();
        };
        pow = arrOperator(pow);

        BinaryOperator<DoubleExpression> max = (op1, op2) -> {
            Pair<Double, Double> doublePair = validate(op1, op2, Double.MIN_VALUE);
            return DoubleValueExpression.builder()
                .value(Math.max(doublePair.getLeft(), doublePair.getRight()))
                .build();
        };
        max = arrOperator(max);

        BinaryOperator<DoubleExpression> min = (op1, op2) ->  {
            Pair<Double, Double> doublePair = validate(op1, op2, Double.MAX_VALUE);
            return DoubleValueExpression.builder()
                .value(Math.min(doublePair.getLeft(), doublePair.getRight()))
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

    private BinaryOperator<DoubleExpression> arrOperator(BinaryOperator<DoubleExpression> operator) {
        return  (op1, op2) -> {
            Double[] values1 = op1.doubleValues();
            Double[] values2 = op2.doubleValues();

            int resultLength = Math.min(values1.length, values2.length);
            Double[] results = new Double[resultLength];
            for (int i=0; i<resultLength; i++) {
                results[i] = operator.apply(
                    DoubleValueExpression.builder().value(values1[i]).build(),
                    DoubleValueExpression.builder().value(values2[i]).build()
                ).doubleValue();
            }

            DoubleValueExpression expression = DoubleValueExpression.builder()
                .values(results)
                .value(operator.apply(op1, op2).doubleValue())
                .build();

            return expression;
        };
    }

    private Pair<Double, Double> validate(DoubleExpression op1, DoubleExpression op2, Double def) {
        Double v1 = op1.doubleValue();
        Double v2 = op2.doubleValue();
        if (v1==null || v2==null) {
            if (v1==null) v1 = def;
            if (v2==null) v2 = def;
        }
        return Pair.of(v1, v2);
    }

    private void unaryOperators() {
        UnaryOperator<DoubleExpression> nothing = (op) -> DoubleValueExpression.builder()
            .value(op == null ? null : op.doubleValue())
            .build();

        UnaryOperator<DoubleExpression> minus = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : -1*op.doubleValue())
            .build();

        UnaryOperator<DoubleExpression> abs = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.abs(op.doubleValue()))
            .build();

        UnaryOperator<DoubleExpression> ceil = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.ceil(op.doubleValue()))
            .build();

        UnaryOperator<DoubleExpression> floor = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.floor(op.doubleValue()))
            .build();

        UnaryOperator<DoubleExpression> sqrt = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.sqrt(op.doubleValue()))
            .build();

        UnaryOperator<DoubleExpression> pow2 = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.pow(op.doubleValue(),2))
            .build();

        UnaryOperator<DoubleExpression> sign = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.signum(op.doubleValue()))
            .build();

        UnaryOperator<DoubleExpression> round = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.round(op.doubleValue()*1d) / 1d)
            .build();

        UnaryOperator<DoubleExpression> round1 = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.round(op.doubleValue()*10d) / 10d)
            .build();

        UnaryOperator<DoubleExpression> round2 = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.round(op.doubleValue()*100d) / 100d)
            .build();

        UnaryOperator<DoubleExpression> round3 = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.round(op.doubleValue()*1000d) / 1000d)
            .build();

        UnaryOperator<DoubleExpression> round4 = (op) -> DoubleValueExpression.builder()
            .value(op.doubleValue() == null ? null : Math.round(op.doubleValue()*10000d) / 10000d)
            .build();

        UnaryOperator<DoubleExpression> sin = (op) -> DoubleValueExpression.builder()
            .value(op == null ? null : Math.sin(op.doubleValue()))
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
        unaryOperators.put("sin", sin);
    }
}
