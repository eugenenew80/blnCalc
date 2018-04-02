package kz.kegoc.bln;

import kz.kegoc.bln.calc.*;
import kz.kegoc.bln.calc.expression.BinaryExpression;
import kz.kegoc.bln.calc.expression.Expression;
import kz.kegoc.bln.calc.operand.DoubleValueOperand;
import kz.kegoc.bln.calc.operand.Operand;
import kz.kegoc.bln.calc.operand.PeriodTimeValueOperand;
import kz.kegoc.bln.calc.service.FormulaCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

@EntityScan(
    basePackageClasses = { App.class, Jsr310JpaConverters.class }
)
@SpringBootApplication
public class App implements ApplicationListener<ApplicationReadyEvent> {
    private static CalcContext context;
    private static Map<String, BinaryOperator<Operand>> binaryOperators = null;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        context = new CalcContext();
        context.setDate(LocalDateTime.of(2018, 3, 28, 0, 0, 0));
        context.setStart(LocalDateTime.of(2018, 3, 28, 0, 0, 0));
        context.setEnd(LocalDateTime.of(2018, 3, 29, 0, 0, 0));

        try {
            String fileName = "C:/src/idea/bln/blnCalc/src/main/resources/formula/test3.xml";
            Double result = formula.calc(Paths.get(fileName), context);
            System.out.println(result);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private FormulaCalculator formula;
}



