package calc.formula.calculation;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.RowTypeEnum;
import calc.entity.calc.source.*;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextType;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.CalcService;
import calc.formula.service.MessageService;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.calc.SourceResultHeaderRepo;
import calc.repo.calc.SourceResultLineRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static calc.util.Util.round;

@SuppressWarnings({"Duplicates", "ImplicitSubclassInspection"})
@Service
@RequiredArgsConstructor
public class SourceService {
    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);
    private static final String docCode = "SOURCE";
    private final SourceResultHeaderRepo sourceResultHeaderRepo;
    private final SourceResultLineRepo sourceResultLineRepo;
    private final MessageService messageService;
    private final PeriodTimeValueService periodTimeValueService;
    private final CalcService calcService;


    public boolean calc(Long headerId) {
        logger.info("Energy source balance for header " + headerId + " started");
        SourceResultHeader header = sourceResultHeaderRepo.findOne(headerId);
        if (header.getStatus() == BatchStatusEnum.E)
            return false;

        CalcContext context = CalcContext.builder()
            .lang(LangEnum.RU)
            .docCode(docCode)
            .headerId(header.getId())
            .periodType(header.getPeriodType())
            .startDate(header.getStartDate())
            .endDate(header.getEndDate())
            .orgId(header.getOrganization().getId())
            .contextType(ContextType.SOURCE)
            .build();

        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header);

            CalcResult result = calcService.calcMeteringPoint(header.getFormula(), context);
            Double value = result !=null ? result.getDoubleValue() : null;
            value = round(value, 0);
            header.setDeliveryVal(value);

            copyGroups(header);
            calcRows(header, context);
            setParents(header);

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);

            updateStatus(header, BatchStatusEnum.C);
            logger.info("Energy source balance for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", new HashMap<>());
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Energy source balance for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void copyGroups(SourceResultHeader header) {
        List<SourceResultLine> resultLines = new ArrayList<>();
        for (SourceLine line : header.getHeader().getLines()) {
            if (line.getRowType() != RowTypeEnum.GROUP)
                continue;

            SourceResultLine resultLine = new SourceResultLine();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setParam(line.getParam());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setRowType(line.getRowType());
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(LocalDateTime.now());
            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines(resultLines);
    }

    private void calcRows(SourceResultHeader header, CalcContext context) {
        List<SourceResultLine> resultLines = new ArrayList<>();
        for (SourceLine line : header.getHeader().getLines()) {
            if (line.getRowType() != RowTypeEnum.ROW)
                continue;

            Map<String, String> msgParams = buildMsgParams(line);
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();

            if (meteringPoint == null) {
                messageService.addMessage(header, line.getLineNum(), docCode, "ES_MP_NOT_FOUND", msgParams);
                continue;
            }

            if (param == null) {
                messageService.addMessage(header, line.getLineNum(), docCode, "ES_PARAM_NOT_FOUND", msgParams);
                continue;
            }

            SourceResultLine resultLine = new SourceResultLine();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setParam(line.getParam());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setRowType(line.getRowType());
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(LocalDateTime.now());
            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines(resultLines);
    }

    private void copyTranslates(SourceLine line, SourceResultLine resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (SourceLineTranslate lineTranslate : line.getTranslates()) {
            SourceResultLineTranslate resultLineTranslate = new SourceResultLineTranslate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);

            resultLineTranslate.setName(lineTranslate.getName());
            if (resultLineTranslate == null && resultLine.getMeteringPoint()!=null)
                resultLineTranslate.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines(List<SourceResultLine> lines) {
        sourceResultLineRepo.save(lines);
        sourceResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void deleteLines(SourceResultHeader header) {
        List<SourceResultLine> lines = sourceResultLineRepo.findAllByHeaderId(header.getId());
        sourceResultLineRepo.delete(lines);
        sourceResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(SourceResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void setParents(SourceResultHeader header) {
        List<SourceResultLine> resultLines = sourceResultLineRepo.findAllByHeaderId(header.getId());
        List<SourceLine> sourceLines = header.getHeader().getLines();

        for (SourceLine sourceLine : sourceLines) {
            if (sourceLine.getParent() == null)
                continue;

            SourceResultLine line = resultLines.stream()
                .filter(t -> t.getLineNum().equals(sourceLine.getLineNum()))
                .findFirst()
                .orElse(null);

            SourceResultLine parentLine = resultLines.stream()
                .filter(t -> t.getLineNum().equals(sourceLine.getParent().getLineNum()))
                .findFirst()
                .orElse(null);

            if (line != null && parentLine!= null)
                line.setParent(parentLine);
        }

        sourceResultLineRepo.save(resultLines);
        sourceResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateStatus(SourceResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        sourceResultHeaderRepo.save(header);
        sourceResultHeaderRepo.flush();
    }

    private Map<String, String> buildMsgParams(SourceLine line) {
        Map<String, String> msgParams = new HashMap<>();
        msgParams.put("line", line.getLineNum().toString());
        return msgParams;
    }
}
