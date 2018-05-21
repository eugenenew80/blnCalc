package calc.rep.impl;

import calc.entity.rep.*;
import calc.entity.rep.enums.ValueTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.service.CalcService;
import calc.rep.DocumentBuilder;
import calc.rep.ReportExecutorService;
import calc.repo.rep.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportExecutorServiceImpl implements ReportExecutorService {
    private final ReportRepo reportRepo;
    private final CalcService calcService;
    private final DocumentBuilder documentBuilder;

    @Override
    public Document buildReport(Long reportId, CalcContext context) throws Exception {
        Report report = reportRepo.findOne(reportId);
        if (report==null)
            throw new Exception("Entity not found");

        Map<Long, CalcResult> results = calc(report, context);
        context.setResults(results);
        return documentBuilder.buildDocument(report, context);
    }

    @Override
    public Map<Long, CalcResult> calc(Report report, CalcContext context) {
        List<TableCell> cells = report.getSheets()
            .stream()
            .flatMap(t -> t.getTables().stream())
            .flatMap(t -> t.getDivisions().stream())
            .flatMap(t -> t.getRows().stream())
            .flatMap(t -> t.getCells().stream())
            .collect(Collectors.toList());

        Map<Long, CalcResult> results = new HashMap<>();
        for (TableCell cell : cells) {
            if (cell.getAttr().getValueType()== ValueTypeEnum.FORMULA && cell.getFormula()!=null) {
                CalcResult result = null;
                try {
                    result = calcService.calc(cell.getFormula(), context);
                }
                catch (Exception e) {
                    results.put(cell.getId(), null);
                    e.printStackTrace();
                }

                results.put(cell.getId(), result);
            }
        }
        return results;
    }

    @Override
    public Map<Long, CalcResult> calc(Long reportId, CalcContext context) {
        Report report = reportRepo.findOne(reportId);
        return calc(report, context);
    }

    @Override
    public void save(Document doc, String fileName) throws Exception {
        DOMSource source = new DOMSource(doc);

        FileWriter writer = new FileWriter(new File(fileName));
        StreamResult result = new StreamResult(writer);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
    }

    @Override
    public Document transform(Document doc, Source xslSource) throws Exception {
        Transformer trans = TransformerFactory
            .newInstance()
            .newTransformer(xslSource);

        DOMSource source = new DOMSource(doc);
        DOMResult output = new DOMResult();
        trans.transform(source, output);

        return (Document) output.getNode();
    }

    @Override
    public Document transform(Document doc) throws Exception {
        return transform(doc, new StreamSource(new File("files/report.xsl")));
    }
}
