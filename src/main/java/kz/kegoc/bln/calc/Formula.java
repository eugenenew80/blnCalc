package kz.kegoc.bln.calc;

import kz.kegoc.bln.calc.expression.BinaryExpression;
import kz.kegoc.bln.calc.expression.Expression;
import kz.kegoc.bln.calc.expression.UnaryExpression;
import kz.kegoc.bln.calc.operand.*;
import kz.kegoc.bln.calc.service.AtTimeValueService;
import kz.kegoc.bln.calc.service.PeriodTimeValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.script.ScriptEngine;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class Formula {
    private final ScriptEngine engine;
    private final Map<String, BinaryOperator<Operand>> binaryOperators;
    private final Map<String, UnaryOperator<Operand>> unaryOperators;
    private final PeriodTimeValueService periodTimeValueService;
    private final AtTimeValueService atTimeValueService;

    public Operand calc(CalcContext context) {
        PeriodTimeValueOperand.PeriodTimeValueOperandBuilder ptOperandBuilder = PeriodTimeValueOperand.builder()
            .service(periodTimeValueService)
            .context(context)
            .rate(1d)
            .meteringPointCode("121420300070100018");

        UnaryExpression activePlus =
            ptOperandBuilder.parameterCode("A+")
                .build()
                .andThen(unaryOperators.get("pow2"));

        UnaryExpression activeMinus = ptOperandBuilder.parameterCode("A-")
            .build()
            .andThen(unaryOperators.get("pow2"));

        UnaryExpression expression = BinaryExpression.builder()
            .operator(binaryOperators.get("add"))
            .operands(Arrays.asList(activePlus, activeMinus))
            .build()
            .andThen(unaryOperators.get("sqrt"))
            .andThen(unaryOperators.get("round4"));

        Double result = expression.getValue();

        /*
        Double result = BinaryExpression.builder()
            .operator(binaryOperators.get("add"))
            .operands(Arrays.asList(
                DoubleValueOperand.builder().value(10d).build(),
                DoubleValueOperand.builder().value(8d).build(),
                DoubleValueOperand.builder().value(15d).build(),
                DoubleValueOperand.builder().value(7d).build()
            ))
            .build()
            .andThen(unaryOperators.get("sqrt"))
            .andThen(unaryOperators.get("round2"))
            .andThen(unaryOperators.get("sign"))
            .getValue();
        */

        return DoubleValueOperand.builder()
            .value(result)
            .build();
    }


    public Expression loadFromXML(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);


        return null;
    }
}
