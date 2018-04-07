package calc.formula.builder.impl;

import calc.formula.CalcContext;
import calc.formula.builder.ExpressionBuilder;
import calc.formula.expression.impl.DoubleValueOperand;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class DoubleValueExpressionBuilder implements ExpressionBuilder<DoubleValueOperand> {

    @Override
    public DoubleValueOperand build(Node node, CalcContext context) {
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
}
