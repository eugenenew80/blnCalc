package calc.formula;

import calc.formula.builder.impl.RootExpressionBuilder;
import calc.formula.expression.Expression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@Service
@RequiredArgsConstructor
public class FormulaCalculatorImpl implements FormulaCalculator {
    private final RootExpressionBuilder expressionBuilder;

    @Override
    public Double calc(String formula, CalcContext context) throws Exception {
        return compile(formula, context).getValue();
    }

    private Expression compile(String formula, CalcContext context) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(formula)));

        Node node = document.getDocumentElement()
            .getParentNode()
            .getFirstChild();

        return expressionBuilder.build(node, context);
    }
}
