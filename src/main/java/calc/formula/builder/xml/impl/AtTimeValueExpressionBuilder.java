package calc.formula.builder.xml.impl;

import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.impl.AtTimeValueExpression;
import calc.formula.service.AtTimeValueService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class AtTimeValueExpressionBuilder implements ExpressionBuilder<AtTimeValueExpression> {
    private final AtTimeValueService service;

    @Override
    public AtTimeValueExpression build(Node node, CalcContext context) {
        return build(node, null, context);
    }

    @Override
    public AtTimeValueExpression build(Node node, String parameterCode, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        String mp = "";
        String param = "";
        String per = "end";
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
                case "per":
                    per = attrValue;
                    break;
                case "rate":
                    rate = Double.parseDouble(attrValue);
                    break;
            }
        }

        if (StringUtils.isEmpty(param))
            param = parameterCode;

        return  AtTimeValueExpression.builder()
            .meteringPointCode(mp)
            .parameterCode(param)
            .per(per)
            .rate(rate)
            .service(service)
            .context(context)
            .build();
    }
}
