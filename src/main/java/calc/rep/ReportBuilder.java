package calc.rep;

import calc.entity.rep.Report;
import calc.entity.rep.TableSection;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import org.w3c.dom.Document;
import javax.xml.transform.Source;
import java.util.List;
import java.util.Map;

public interface ReportBuilder {
    Document buildReport(Long reportId, CalcContext context) throws Exception;

    Document transform(Document doc, Source xslSource) throws Exception;

    Document transform(Document doc) throws Exception;

    Map<Long, CalcResult> calc(Report report, CalcContext context);

    Map<Long, CalcResult> calc(Long reportId, CalcContext context);

    void save(Document doc, String fileName) throws Exception;

    Report createFromTemplate(Long reportId);

    void addRows(Long sectionId, List<String> keys);

    void generateCells(Long sectionId, String paramCode);
}
