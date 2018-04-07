package calc.formula.builder.xml;

import calc.formula.CalcContext;
import calc.formula.operand.JavaScriptOperand;
import calc.formula.operand.Operand;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.Node;
import javax.script.ScriptEngine;
import java.util.HashMap;

@RequiredArgsConstructor
public class JavaScriptOperandBuilder implements OperandBuilder<JavaScriptOperand> {
    private final ScriptEngine engine;
    private final ExpressionBuilder expressionBuilder;

    @Override
    public JavaScriptOperand build(Node parentNode, CalcContext context) {
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
                        Operand operand = expressionBuilder.build(operandNode, context);
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
