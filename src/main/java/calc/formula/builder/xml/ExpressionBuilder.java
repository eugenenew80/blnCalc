package calc.formula.builder.xml;

import calc.formula.CalcContext;
import calc.formula.expression.BinaryExpression;
import calc.formula.expression.Expression;
import calc.formula.expression.UnaryExpression;
import calc.formula.builder.factory.OperandBuilderFactory;
import calc.formula.operand.DoubleValueOperand;
import calc.formula.operand.Operand;
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
public class ExpressionBuilder implements OperandBuilder<Operand> {
    private final OperandBuilderFactory operandBuilderFactory;
    private final Map<String, BinaryOperator<Operand>> binaryOperators;
    private final Map<String, UnaryOperator<Operand>> unaryOperators;

    @Override
    public Operand build(Node node, CalcContext context) {
        String nodeType = getNodeType(node);
        if (nodeType.equals("binary"))
            return buildBinary(node, context);

        if (nodeType.equals("unary"))
            return buildUnary(node, context);

        if (nodeType.equals("operand"))
            return buildOperand(node, context);

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
            .operand(DoubleValueOperand.builder().value(0d).build())
            .build();
    }

    private  Expression buildBinary(Node node, CalcContext context) {
        BinaryOperator<Operand> binaryOperator = binaryOperators.get(node.getNodeName());
        List<Operand> operands = buildOperands(node, context);

        BinaryExpression expression = BinaryExpression.builder()
            .operator(binaryOperator)
            .operands(operands)
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

        UnaryOperator<Operand> operator = unaryOperators.get(node.getNodeName());
        Operand operand = buildOperand(node.getChildNodes().item(k), context);

        UnaryExpression expression = UnaryExpression.builder()
            .operator(operator)
            .operand(operand)
            .build();

        return expression;
    }

    private List<Operand> buildOperands(Node node, CalcContext context) {
        List<Operand> operands = new ArrayList<>();
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==3) continue;
            Operand operand = buildOperand(node.getChildNodes().item(i), context);
            operands.add(operand);
        }

        return operands;
    }

    private  Operand buildOperand(Node node, CalcContext context) {
        OperandBuilder operandBuilder = operandBuilderFactory.getBuilder(node.getNodeName(), this);
        if (operandBuilder!=null)
            return operandBuilder.build(node, context);

        return build(node, context);
    }
}
