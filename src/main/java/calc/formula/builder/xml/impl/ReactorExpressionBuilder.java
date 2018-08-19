package calc.formula.builder.xml.impl;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.impl.ReactorExpression;
import calc.formula.service.ReactorService;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class ReactorExpressionBuilder implements ExpressionBuilder<ReactorExpression> {
    private final ReactorService service;

    @Override
    public ReactorExpression build(Node node, Formula formula, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        Long id = null;
        String attr = "";
        //noinspection Duplicates
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

        return  ReactorExpression.builder()
            .id(id)
            .attr(attr)
            .context(context)
            .service(service)
            .build();
    }
}
