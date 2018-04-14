package calc.formula.service.impl;

import calc.entity.Formula;
import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.impl.BinaryExpression;
import calc.formula.expression.Expression;
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
    public Expression parse(Node node, Formula formula, CalcContext context) {
        String nodeType = getNodeType(node);
        if (nodeType.equals("binary"))
            return buildBinary(node, formula, context);

        if (nodeType.equals("unary"))
            return buildUnary(node, formula, context);

        if (nodeType.equals("expression"))
            return buildExpression(node, formula, context);

        throw new IllegalArgumentException("Invalid operation: " + node.getNodeName());
    }


    @Override
    public Expression parse(Formula formula, CalcContext context) throws Exception {
        Node node = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder().parse(new InputSource(new StringReader(formula.getText())))
            .getDocumentElement()
            .getParentNode()
            .getFirstChild();

        return parse(node, formula, context);
    }


    private String getNodeType(Node node) {
        String operator = node.getNodeName();
        if (operatorFactory.binary(operator)!=null)
            return "binary";

        if (operatorFactory.unary(operator)!=null)
            return "unary";

        return "expression";
    }

    private  Expression buildBinary(Node node, Formula formula, CalcContext context) {
        BinaryOperator<Expression> binaryOperator = operatorFactory.binary(node.getNodeName());
        List<Expression> expressions = buildExpressions(node, formula, context);

        BinaryExpression expression = BinaryExpression.builder()
            .operator(binaryOperator)
            .expressions(expressions)
            .build();

        return expression;
    }

    private Expression buildUnary(Node node, Formula formula, CalcContext context) {
        int k=-1;
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==1) {
                k=i;
                break;
            }
        }

        UnaryOperator<Expression> operator = operatorFactory.unary(node.getNodeName());
        Expression expression = buildExpression(node.getChildNodes().item(k), formula, context);
        return expression.andThen(operator);
    }

    private List<Expression> buildExpressions(Node node, Formula formula, CalcContext context) {
        List<Expression> expressions = new ArrayList<>();
        for (int i=0; i<node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeType()==3) continue;
            Expression expression = buildExpression(node.getChildNodes().item(i), formula, context);
            expressions.add(expression);
        }

        return expressions;
    }

    private Expression buildExpression(Node node, Formula formula, CalcContext context) {
        ExpressionBuilder builder = builderFactory.getBuilder(node.getNodeName(), this);
        if (builder!=null)
            return builder.build(node, formula, context);

        return parse(node, formula, context);
    }


    public List<String> sort(Map<String, Expression> expressionMap) throws Exception {
        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        for (String key : expressionMap.keySet())
            graph.addVertex(key);

        for (String key : expressionMap.keySet()) {
            Expression expression = expressionMap.get(key);
            for (String mp : expression.meteringPoints()) {
                if (graph.containsVertex(mp))
                    graph.addEdge(mp, key);
            }
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
