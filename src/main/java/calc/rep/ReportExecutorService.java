package calc.rep;

import calc.entity.rep.Report;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import org.w3c.dom.Document;
import javax.xml.transform.Source;
import java.util.Map;

public interface ReportExecutorService {
    Document buildReport(Long reportId, CalcContext context) throws Exception;

    Document transform(Document doc, Source xslSource) throws Exception;

    Document transform(Document doc) throws Exception;

    Map<Long, CalcResult> calc(Report report, CalcContext context);

    Map<Long, CalcResult> calc(Long reportId, CalcContext context);

    void save(Document doc, String fileName) throws Exception;
}
