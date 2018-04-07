package calc.formula.builder.impl;

import calc.formula.CalcContext;
import calc.formula.builder.ExpressionBuilder;
import calc.formula.expression.impl.BinaryExpression;
import calc.formula.expression.Expression;
import calc.formula.expression.impl.UnaryExpression;
import calc.formula.builder.ExpressionBuilderFactory;
import calc.formula.expression.impl.DoubleValueOperand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class RootExpressionBuilder implements ExpressionBuilder<Expression> {
    private final ExpressionBuilderFactory operandBuilderFactory;
    private final Map<String, BinaryOperator<Expression>> binaryOperators;
    private final Map<String, UnaryOperator<Expression>> unaryOperators;

    @Override
    public Expression build(Node node, CalcContext context) {
        String nodeType = getNodeType(node);
        if (nodeType.equals("binary"))
            return buildBinary(node, context);

        if (nodeType.equals("unary"))
            return buildUnary(node, context);

        if (nodeType.equals("operand"))
            return buildExpression(node, context);

        return defaultExpression();
    }

    private String getNodeType(Node node) {
        String operator = node.getNodeName();
        if (binaryOperators.containsKey(operator))
            return "binary";

        if (unaryOperators.containsKey(operator))
            return "unary";

        return "operand";
    }

    private Expression defaultExpression() {
        return UnaryExpression.builder()
            .operator(unaryOperators.get("nothing"))
            .expression(DoubleValueOperand.builder().value(0d).build())
            .build();
    }

    private  Expression buildBinary(Node node, CalcContext context) {
        BinaryOperator<Expression> binaryOperator = binaryOperators.get(node.getNodeName());
        List<Expression> expressions = buildExpressions(node, context);

        BinaryExpression expression = BinaryExpression.builder()
            .operator(binaryOperator)
            .expressions(expressions)
            .build();

        return expression;
    }

    private Expression buildUnary(Node node, CalcContext context) {
        int k=-1;
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==1) {
                k=i;
                break;
            }
        }

        UnaryOperator<Expression> operator = unaryOperators.get(node.getNodeName());
        Expression expression = buildExpression(node.getChildNodes().item(k), context);
        return expression.andThen(operator);
    }

    private List<Expression> buildExpressions(Node node, CalcContext context) {
        List<Expression> expressions = new ArrayList<>();
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==3) continue;
            Expression expression = buildExpression(node.getChildNodes().item(i), context);
            expressions.add(expression);
        }

        return expressions;
    }

    private Expression buildExpression(Node node, CalcContext context) {
        ExpressionBuilder expressionBuilder = operandBuilderFactory.getBuilder(node.getNodeName(), this);
        if (expressionBuilder!=null)
            return expressionBuilder.build(node, context);

        return build(node, context);
    }
}
