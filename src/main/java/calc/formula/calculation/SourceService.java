package calc.formula.calculation;

import calc.entity.calc.Formula;
import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.distr.DistrResultHeader;
import calc.entity.calc.distr.DistrResultLine;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.RowTypeEnum;
import calc.entity.calc.source.*;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextType;
import calc.formula.service.CalcService;
import calc.formula.service.MessageService;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.calc.DistrResultHeaderRepo;
import calc.repo.calc.SourceResultHeaderRepo;
import calc.repo.calc.SourceResultLine1Repo;
import calc.repo.calc.SourceResultLine2Repo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static calc.util.Util.round;
import static java.util.stream.Collectors.toList;

@SuppressWarnings({"Duplicates", "ImplicitSubclassInspection"})
@Service
@RequiredArgsConstructor
public class SourceService {
    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);
    private static final String docCode = "SOURCE";
    private final SourceResultHeaderRepo sourceResultHeaderRepo;
    private final SourceResultLine1Repo sourceResultLine1Repo;
    private final SourceResultLine2Repo sourceResultLine2Repo;
    private final DistrResultHeaderRepo distrResultHeaderRepo;
    private final MessageService messageService;
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

            calcLines1(header, context);
            copyGroups(header);
            calcLines2(header, context);
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
        List<SourceResultLine2> resultLines = new ArrayList<>();
        for (SourceLine line : header.getHeader().getLines()) {
            if (line.getRowType() != RowTypeEnum.GROUP)
                continue;

            SourceResultLine2 resultLine = new SourceResultLine2();
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
        saveLines2(resultLines);
    }

    private void calcLines1(SourceResultHeader header, CalcContext context) {
        Formula formula = header.getFormula();
        if (formula == null)
            return;

        if (header.getMeteringPoint() == null)
            return;

        if (header.getParam() == null)
            return;

        List<MeteringPoint> lines = formula.getVars()
            .stream()
            .flatMap(t -> t.getDetails().stream())
            .map(t -> t.getMeteringPoint())
            .distinct()
            .filter(t -> t != null)
            .collect(toList());

        lines.add(header.getMeteringPoint());

        List<SourceResultLine1> resultLines = new ArrayList<>();
        Long lineNum = 0l;
        for (MeteringPoint line : lines) {
            lineNum++;

            Double val = null;
            try {
                CalcResult result = calcService.calcMeteringPoint(line, formula.getParam(), formula.getParamType(), context);
                val = result != null ? result.getDoubleValue() : null;
                val = round(val, 0);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            SourceResultLine1 resultLine = new SourceResultLine1();
            resultLine.setHeader(header);
            resultLine.setLineNum(lineNum);
            resultLine.setMeteringPoint(line);
            resultLine.setParam(formula.getParam());
            resultLine.setTotalVal(val);
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(LocalDateTime.now());
            resultLines.add(resultLine);
        }
        saveLines1(resultLines);
    }

    private void calcLines2(SourceResultHeader header, CalcContext context) {
        List<SourceResultLine2> resultLines = new ArrayList<>();
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

            List<DistrResultHeader> distrList = distrResultHeaderRepo.findByOrg(header.getOrganization().getId(), header.getDataType().name(), header.getStartDate(), header.getEndDate());
            DistrResultLine distrLine = distrList.stream()
                .flatMap(t -> t.getLines().stream())
                .filter(t -> t.getMeteringPoint() != null)
                .filter(t -> t.getParam() != null)
                .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                .filter(t -> t.getParam().equals(param))
                .findFirst()
                .orElse(null);

            SourceResultLine2 resultLine = new SourceResultLine2();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setParam(line.getParam());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setRowType(line.getRowType());

            if (distrLine != null) {
                resultLine.setOwnVal(distrLine.getOwnVal());
                resultLine.setOtherVal(distrLine.getOtherVal());
                resultLine.setTotalVal(distrLine.getTotalVal());
            }

            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(LocalDateTime.now());
            copyTranslates(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines2(resultLines);
    }

    private void copyTranslates(SourceLine line, SourceResultLine2 resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (SourceLineTranslate lineTranslate : line.getTranslates()) {
            SourceResultLine1Translate resultLineTranslate = new SourceResultLine1Translate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);

            resultLineTranslate.setName(lineTranslate.getName());
            if (resultLineTranslate == null && resultLine.getMeteringPoint()!=null)
                resultLineTranslate.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines1(List<SourceResultLine1> lines) {
        sourceResultLine1Repo.save(lines);
        sourceResultLine1Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines2(List<SourceResultLine2> lines) {
        sourceResultLine2Repo.save(lines);
        sourceResultLine2Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void deleteLines(SourceResultHeader header) {
        List<SourceResultLine2> lines = sourceResultLine2Repo.findAllByHeaderId(header.getId());
        sourceResultLine2Repo.delete(lines);
        sourceResultLine2Repo.flush();


    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(SourceResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void setParents(SourceResultHeader header) {
        List<SourceResultLine2> resultLines = sourceResultLine2Repo.findAllByHeaderId(header.getId());
        List<SourceLine> sourceLines = header.getHeader().getLines();

        for (SourceLine sourceLine : sourceLines) {
            if (sourceLine.getParent() == null)
                continue;

            SourceResultLine2 line = resultLines.stream()
                .filter(t -> t.getLineNum().equals(sourceLine.getLineNum()))
                .findFirst()
                .orElse(null);

            SourceResultLine2 parentLine = resultLines.stream()
                .filter(t -> t.getLineNum().equals(sourceLine.getParent().getLineNum()))
                .findFirst()
                .orElse(null);

            if (line != null && parentLine!= null)
                line.setParent(parentLine);
        }

        sourceResultLine2Repo.save(resultLines);
        sourceResultLine2Repo.flush();
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
