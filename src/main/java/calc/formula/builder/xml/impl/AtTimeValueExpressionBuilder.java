package calc.formula.builder.xml.impl;

import calc.entity.Formula;
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
    public AtTimeValueExpression build(Node node, Formula formula, CalcContext context) {
        return build(node, formula, null, context);
    }

    @Override
    public AtTimeValueExpression build(Node node, Formula formula, String parameterCode, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        String mp = "";
        String param = "";
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
            .rate(rate)
            .formula(formula)
            .service(service)
            .context(context)
            .build();
    }
}
