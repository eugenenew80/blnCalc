package calc.formula.builder.xml.impl;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.impl.MeteringPointExpression;
import calc.formula.service.MeteringPointService;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class MeteringPointExpressionBuilder implements ExpressionBuilder<MeteringPointExpression> {
    private final MeteringPointService service;

    @Override
    public MeteringPointExpression build(Node node, Formula formula, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        Long id = null;
        String code = "";
        String attr = "";
        //noinspection Duplicates
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

        return  MeteringPointExpression.builder()
            .id(id)
            .code(code)
            .attr(attr)
            .service(service)
            .context(context)
            .build();
    }
}
