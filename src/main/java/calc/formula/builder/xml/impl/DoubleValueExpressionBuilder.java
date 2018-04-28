package calc.formula.builder.xml.impl;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.impl.DoubleValueExpression;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class DoubleValueExpressionBuilder implements ExpressionBuilder<DoubleValueExpression> {

    @Override
    public DoubleValueExpression build(Node node, Formula formula, CalcContext context) {
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

        return DoubleValueExpression.builder()
            .value(val)
            .formula(formula)
            .build();
    }
}
