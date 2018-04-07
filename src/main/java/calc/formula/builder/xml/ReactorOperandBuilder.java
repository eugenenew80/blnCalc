package calc.formula.builder.xml;

import calc.formula.CalcContext;
import calc.formula.operand.ReactorOperand;
import calc.formula.service.ReactorService;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class ReactorOperandBuilder implements OperandBuilder<ReactorOperand> {
    private final ReactorService service;

    @Override
    public ReactorOperand build(Node node, CalcContext context) {
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

        return  ReactorOperand.builder()
            .id(id)
            .attr(attr)
            .context(context)
            .service(service)
            .build();
    }
}
