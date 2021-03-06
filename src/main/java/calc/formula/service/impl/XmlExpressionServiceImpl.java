package calc.formula.service.impl;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.impl.BinaryExpression;
import calc.formula.expression.DoubleExpression;
import calc.formula.builder.ExpressionBuilderFactory;
import calc.formula.service.OperatorFactory;
import calc.formula.service.XmlExpressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class XmlExpressionServiceImpl implements XmlExpressionService {
    private final ExpressionBuilderFactory builderFactory;
    private final OperatorFactory operatorFactory;

    @Override
    public DoubleExpression parse(Node node, String parameterCode, CalcContext context) {
        String nodeType = getNodeType(node);
        if (nodeType.equals("binary"))
            return buildBinary(node, parameterCode, context);

        if (nodeType.equals("unary"))
            return buildUnary(node, parameterCode, context);

        if (nodeType.equals("doubleExpression"))
            return buildExpression(node, parameterCode, context);

        if (nodeType.equals("stringExpression"))
            return buildExpression(node, parameterCode, context);

        throw new IllegalArgumentException("Invalid operation: " + node.getNodeName());
    }


    @Override
    public DoubleExpression parse(Formula formula, String parameterCode, CalcContext context) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        Node node = factory
            .newDocumentBuilder().parse(new InputSource(new StringReader(formula.getText())))
            .getDocumentElement()
            .getParentNode()
            .getFirstChild();

        return parse(node, parameterCode, context);
    }


    private String getNodeType(Node node) {
        String operator = node.getNodeName();
        if (operatorFactory.binary(operator)!=null)
            return "binary";

        if (operatorFactory.unary(operator)!=null)
            return "unary";

        if (operator.equals("mp")) {
            Node attr = node.getAttributes().getNamedItem("attr");
            if (attr.getNodeValue().equals("code") || attr.getNodeValue().equals("name"));
                return "stringExpression";
        }

        return "doubleExpression";
    }

    private DoubleExpression buildBinary(Node node, String parameterCode, CalcContext context) {
        BinaryOperator<DoubleExpression> binaryOperator = operatorFactory.binary(node.getNodeName());
        List<DoubleExpression> expressions = buildExpressions(node, parameterCode, context);

        BinaryExpression expression = BinaryExpression.builder()
            .operator(binaryOperator)
            .expressions(expressions)
            .build();

        return expression;
    }

    private DoubleExpression buildUnary(Node node, String parameterCode, CalcContext context) {
        int k=-1;
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==1) {
                k=i;
                break;
            }
        }

        UnaryOperator<DoubleExpression> operator = operatorFactory.unary(node.getNodeName());
        DoubleExpression expression = buildExpression(node.getChildNodes().item(k), parameterCode, context);
        return expression.andThen(operator);
    }

    private List<DoubleExpression> buildExpressions(Node node, String parameterCode, CalcContext context) {
        List<DoubleExpression> expressions = new ArrayList<>();
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==3) continue;
            DoubleExpression expression = buildExpression(node.getChildNodes().item(i), parameterCode, context);
            expressions.add(expression);
        }

        return expressions;
    }

    private DoubleExpression buildExpression(Node node, String parameterCode, CalcContext context) {
        ExpressionBuilder builder = builderFactory.getBuilder(node.getNodeName(), this);
        if (builder!=null)
            return builder.build(node, parameterCode, context);

        return parse(node, parameterCode, context);
    }
}
