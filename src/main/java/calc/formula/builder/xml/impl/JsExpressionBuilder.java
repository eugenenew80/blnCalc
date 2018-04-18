package calc.formula.builder.xml.impl;

import calc.entity.Formula;
import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.Expression;
import calc.formula.expression.impl.JsExpression;
import calc.formula.service.XmlExpressionService;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Node;
import javax.script.ScriptEngine;
import java.util.HashMap;

@RequiredArgsConstructor
public class JsExpressionBuilder implements ExpressionBuilder<JsExpression> {
    private final ScriptEngine engine;
    private final XmlExpressionService expressionService;

    @Override
    public JsExpression build(Node parentNode, Formula formula, CalcContext context) {
        return build(parentNode, formula, null, context);
    }

    @Override
    public JsExpression build(Node parentNode, Formula formula, String parameterCode, CalcContext context) {
        String src = "";
        HashMap<String, Expression> attributes = new HashMap<>();

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
                        Expression operand = expressionService.parse(operandNode, formula, parameterCode, context);
                        attributes.put(paramName, operand);
                    }
                }
            }
        }

        return  JsExpression.builder()
            .src(src)
            .attributes(attributes)
            .formula(formula)
            .engine(engine)
            .build();
    }
}
