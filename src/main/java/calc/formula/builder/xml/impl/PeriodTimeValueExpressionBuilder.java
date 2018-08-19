package calc.formula.builder.xml.impl;

import calc.entity.calc.Formula;
import calc.formula.CalcContext;
import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.PeriodTimeValueService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class PeriodTimeValueExpressionBuilder implements ExpressionBuilder<PeriodTimeValueExpression> {
    private final PeriodTimeValueService service;

    @Override
    public PeriodTimeValueExpression build(Node node, Formula formula, CalcContext context) {
        return build(node, formula, null, context);
    }

    @Override
    public PeriodTimeValueExpression build(Node node, Formula formula, String parameterCode, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        String mp = "";
        String param = "";
        Double rate = 1d;
        Byte startHour = 0;
        Byte endHour = 23;
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
                case "start":
                    startHour = Byte.parseByte(attrValue);
                    break;
                case "end":
                    endHour = Byte.parseByte(attrValue);
                    break;
            }
        }

        if (StringUtils.isEmpty(param))
            param = parameterCode;

        return PeriodTimeValueExpression.builder()
            .meteringPointCode(mp)
            .parameterCode(param)
            .rate(rate)
            .startHour(startHour)
            .endHour(endHour)
            .service(service)
            .context(context)
            .build();
    }
}
