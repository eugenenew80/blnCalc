package calc.formula;

import calc.formula.builder.xml.ExpressionBuilder;
import calc.formula.expression.Expression;
import calc.formula.expression.UnaryExpression;
import calc.formula.operand.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Map;
import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class FormulaCalculatorImpl implements FormulaCalculator {
    private final Map<String, UnaryOperator<Operand>> unaryOperators;
    private final ExpressionBuilder expressionBuilder;

    @Override
    public Double calc(String formula, CalcContext context) throws Exception {
        Operand expression = compile(formula, context);
        return expression.getValue();
    }

    private Expression compile(String formula, CalcContext context) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(formula)));

        Node node = document.getDocumentElement()
            .getParentNode()
            .getFirstChild();

        Operand operand = expressionBuilder.build(node, context);
        return UnaryExpression.builder()
            .operand(operand)
            .operator(unaryOperators.get("nothing"))
            .build();
    }
}
