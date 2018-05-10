package calc.rep;

import calc.entity.rep.Report;
import calc.formula.CalcContext;
import org.w3c.dom.Document;

public interface DocumentBuilder {
    Document buildDocument(Report report, CalcContext context) throws Exception;
}
