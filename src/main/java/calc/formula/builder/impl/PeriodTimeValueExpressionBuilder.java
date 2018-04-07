package calc.formula.builder.impl;

import calc.formula.CalcContext;
import calc.formula.builder.ExpressionBuilder;
import calc.formula.expression.impl.PeriodTimeValueOperand;
import calc.formula.service.PeriodTimeValueService;
import lombok.RequiredArgsConstructor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@RequiredArgsConstructor
public class PeriodTimeValueExpressionBuilder implements ExpressionBuilder<PeriodTimeValueOperand> {
    private final PeriodTimeValueService service;

    @Override
    public PeriodTimeValueOperand build(Node node, CalcContext context) {
        NamedNodeMap attributes = node.getAttributes();

        String mp = "";
        String param = "";
        String interval = "c";
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
                case "interval":
                    interval = attrValue;
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

        return PeriodTimeValueOperand.builder()
            .meteringPointCode(mp)
            .parameterCode(param)
            .interval(interval)
            .rate(rate)
            .startHour(startHour)
            .endHour(endHour)
            .service(service)
            .context(context)
            .build();
    }
}
