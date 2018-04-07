package calc.formula.builder.xml.impl;

import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.impl.PowerLineExpression;
import calc.formula.service.PowerLineService;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class PowerLineExpressionBuilder implements ExpressionBuilder<PowerLineExpression> {
    private final PowerLineService service;

    @Override
    public PowerLineExpression build(Node node, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        Long id = null;
        String code = "";
        String attr = "";
        for (int i=0; i<attributes.getLength(); i++) {
            String attrName = attributes.item(i).getNodeName();
            String attrValue = attributes.item(i).getNodeValue();
            switch (attrName) {
                case "code":
                    code = attrValue;
                    break;
                case "attr":
                    attr = attrValue;
                    break;
                case "id":
                    id = Long.parseLong(attrValue);
                    break;
            }
        }

        return  PowerLineExpression.builder()
            .id(id)
            .code(code)
            .attr(attr)
            .service(service)
            .context(context)
            .build();
    }
}
