package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;
import kz.kegoc.bln.calc.expression.BinaryExpression;
import kz.kegoc.bln.calc.expression.Expression;
import kz.kegoc.bln.calc.expression.UnaryExpression;
import kz.kegoc.bln.calc.operand.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.script.ScriptEngine;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;


@Service
@RequiredArgsConstructor
public class FormulaCalculatorImpl implements FormulaCalculator {
    private final ScriptEngine engine;
    private final Map<String, BinaryOperator<Operand>> binaryOperators;
    private final Map<String, UnaryOperator<Operand>> unaryOperators;
    private final PeriodTimeValueService periodTimeValueService;
    private final AtTimeValueService atTimeValueService;

    @Override
    public Double calc(String formula, CalcContext context) throws Exception {
        Operand expression = compile(formula, context);
        return expression.getValue();
    }

    @Override
    public Double calc(Path path, CalcContext context) throws Exception {
        byte[] encoded = Files.readAllBytes(path);
        String str = new String(encoded, StandardCharsets.UTF_8);
        return calc(str, context);
    }


    private Expression compile(String formula, CalcContext context) throws Exception {
        DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(formula)));
        Node node = document.getDocumentElement().getParentNode().getFirstChild();

        Operand operand = buildExpression(node, context);

        if (operand instanceof Expression)
            return (Expression) operand;

        return UnaryExpression.builder()
            .operand(operand)
            .operator(unaryOperators.get("nothing"))
            .build();
    }

    private Operand buildExpression(Node node, CalcContext context) {
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
        if (node.getNodeName().equals("pt"))
            return buildPtOperand(node, context);

        if (node.getNodeName().equals("at"))
            return buildAtOperand(node, context);

        if (node.getNodeName().equals("number"))
            return buildDoubleValueOperand(node, context);

        if (node.getNodeName().equals("js"))
            return buildJsOperand(node, context);

        return buildExpression(node, context);
    }

    private Operand buildDoubleValueOperand(Node node, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();
        Double val = 0d;
        for (int i=0; i<attributes.getLength(); i++) {
            String attrName = attributes.item(i).getNodeName();
            String attrValue = attributes.item(i).getNodeValue();
            switch (attrName) {
                case "val":
                    val = Double.parseDouble(attrValue);
                    break;
            }
        }

        return DoubleValueOperand.builder()
            .value(val)
            .build();
    }

    private Operand buildPtOperand(Node node, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        String mp = "";
        String param = "";
        Double rate = 1d;
        for (int i=0; i<attributes.getLength(); i++) {
            String attrName = attributes.item(i).getNodeName();
            String attrValue = attributes.item(i).getNodeValue();
            switch (attrName) {
                case "mp":
                    mp = attrValue;
                    break;
                case "param":
                    param = attrValue;
                    break;
                case "rate":
                    rate = Double.parseDouble(attrValue);
                    break;
            }
        }

        return PeriodTimeValueOperand.builder()
            .meteringPointCode(mp)
            .parameterCode(param)
            .rate(rate)
            .service(periodTimeValueService)
            .context(context)
            .build();
    }

    private Operand buildAtOperand(Node node, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        String mp = "";
        String param = "";
        Double rate = 1d;
        for (int i=0; i<attributes.getLength(); i++) {
            String attrName = attributes.item(i).getNodeName();
            String attrValue = attributes.item(i).getNodeValue();
            switch (attrName) {
                case "mp":
                    mp = attrValue;
                    break;
                case "param":
                    param = attrValue;
                    break;
                case "rate":
                    rate = Double.parseDouble(attrValue);
                    break;
            }
        }

        return  AtTimeValueOperand.builder()
            .meteringPointCode(mp)
            .parameterCode(param)
            .rate(rate)
            .service(atTimeValueService)
            .context(context)
            .build();
    }

    private Operand buildJsOperand(Node parentNode, CalcContext context) {
        String src = "";
        HashMap<String, Operand> attributes = new HashMap<>();

        for (int i = 0; i < parentNode.getChildNodes().getLength(); i++) {
            Node node = parentNode.getChildNodes().item(i);
            if (node.getNodeName().equals("src"))
                src = node.getTextContent();

            if (node.getNodeName().equals("params")) {
                for (int j = 0; j<node.getChildNodes().getLength(); j++) {
                    if (node.getChildNodes().item(j).getNodeType() == Node.TEXT_NODE)
                        continue;

                    Node paramNode = node.getChildNodes().item(j);
                    String paramName = paramNode.getAttributes()
                        .getNamedItem("name")
                        .getNodeValue();

                    for (int k=0; k<paramNode.getChildNodes().getLength(); k++) {
                        if (paramNode.getChildNodes().item(k).getNodeType() == Node.TEXT_NODE)
                            continue;

                        Node operandNode = paramNode.getChildNodes().item(k);
                        Operand operand = buildOperand(operandNode, context);
                        attributes.put(paramName, operand);
                    }
                }
            }
        }

        return  JavaScriptOperand.builder()
            .src(src)
            .attributes(attributes)
            .engine(engine)
            .build();
    }
}
