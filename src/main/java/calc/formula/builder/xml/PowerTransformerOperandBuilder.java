package calc.formula.builder.xml;

import calc.formula.CalcContext;
import calc.formula.operand.PowerTransformerOperand;
import calc.formula.service.PowerTransformerService;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class PowerTransformerOperandBuilder implements OperandBuilder<PowerTransformerOperand> {
    private final PowerTransformerService service;

    @Override
    public PowerTransformerOperand build(Node node, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        Long id = null;
        String attr = "";
        for (int i=0; i<attributes.getLength(); i++) {
            String attrName = attributes.item(i).getNodeName();
            String attrValue = attributes.item(i).getNodeValue();
            switch (attrName) {
                case "id":
                    id = Long.parseLong(attrValue);
                    break;
                case "attr":
                    attr = attrValue;
                    break;
            }
        }

        return  PowerTransformerOperand.builder()
            .id(id)
            .attr(attr)
            .service(service)
            .context(context)
            .build();
    }
}
