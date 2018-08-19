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
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class XmlExpressionServiceImpl implements XmlExpressionService {
    private final ExpressionBuilderFactory builderFactory;
    private final OperatorFactory operatorFactory;

    @Override
    public DoubleExpression parse(Node node, Formula formula, String parameterCode, CalcContext context) {
        String nodeType = getNodeType(node);
        if (nodeType.equals("binary"))
            return buildBinary(node, formula, parameterCode, context);

        if (nodeType.equals("unary"))
            return buildUnary(node, formula, parameterCode, context);

        if (nodeType.equals("doubleExpression"))
            return buildExpression(node, formula, parameterCode, context);

        if (nodeType.equals("stringExpression"))
            return buildExpression(node, formula, parameterCode, context);

        throw new IllegalArgumentException("Invalid operation: " + node.getNodeName());
    }

    @Override
    public DoubleExpression parse(Formula formula, CalcContext context) throws Exception {
        return parse(formula, null, context);
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

        return parse(node, formula, parameterCode, context);
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

    private DoubleExpression buildBinary(Node node, Formula formula, String parameterCode, CalcContext context) {
        BinaryOperator<DoubleExpression> binaryOperator = operatorFactory.binary(node.getNodeName());
        List<DoubleExpression> expressions = buildExpressions(node, formula, parameterCode, context);

        BinaryExpression expression = BinaryExpression.builder()
            .operator(binaryOperator)
            .expressions(expressions)
            .build();

        return expression;
    }

    private DoubleExpression buildUnary(Node node, Formula formula, String parameterCode, CalcContext context) {
        int k=-1;
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==1) {
                k=i;
                break;
            }
        }

        UnaryOperator<DoubleExpression> operator = operatorFactory.unary(node.getNodeName());
        DoubleExpression expression = buildExpression(node.getChildNodes().item(k), formula, parameterCode, context);
        return expression.andThen(operator);
    }

    private List<DoubleExpression> buildExpressions(Node node, Formula formula, String parameterCode, CalcContext context) {
        List<DoubleExpression> expressions = new ArrayList<>();
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==3) continue;
            DoubleExpression expression = buildExpression(node.getChildNodes().item(i), formula, parameterCode, context);
            expressions.add(expression);
        }

        return expressions;
    }

    private DoubleExpression buildExpression(Node node, Formula formula, String parameterCode, CalcContext context) {
        ExpressionBuilder builder = builderFactory.getBuilder(node.getNodeName(), this);
        if (builder!=null)
            return builder.build(node, formula, parameterCode, context);

        return parse(node, formula, parameterCode, context);
    }

    public List<String> sort(Map<String, DoubleExpression> expressionMap) throws Exception {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        for (String key : expressionMap.keySet())
            graph.addVertex(key);

        for (String key : expressionMap.keySet()) {
            for (String mp : expressionMap.get(key).codes())
                if (graph.containsVertex(mp)) graph.addEdge(mp, key);
        }

        Set<String> detectedCycles = detectCycles(graph);
        if (!detectedCycles.isEmpty())
            throw new Exception("Cycles detected: " + detectedCycles.iterator().next());

        List<String> ordered = new ArrayList<>();
        TopologicalOrderIterator<String, DefaultEdge> orderIterator = new TopologicalOrderIterator<>(graph);
        while (orderIterator.hasNext())
            ordered.add(orderIterator.next());

        return ordered;
    }

    private Set<String> detectCycles(DefaultDirectedGraph<String, DefaultEdge> graph) {
        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(graph);
        return cycleDetector.findCycles();
    }
}
