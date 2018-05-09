package calc.rep;

import calc.formula.CalcContext;
import org.w3c.dom.Document;
import javax.xml.transform.Source;

public interface ReportBuilder {
    Document buildReport(Long reportId, CalcContext context) throws Exception;

    Document transform(Document doc, Source xslSource) throws Exception;

    Document transform(Document doc) throws Exception;

    void save(Document doc, String fileName) throws Exception;
}
