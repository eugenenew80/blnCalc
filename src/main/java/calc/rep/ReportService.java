package calc.rep;

import org.w3c.dom.Document;

import javax.xml.transform.Source;

public interface ReportService {
    Document buildReport(Long reportId) throws Exception;
    Document transform(Document doc, Source xslSource) throws Exception;
    Document transform(Long reportId, Source xslSource) throws Exception;
}
