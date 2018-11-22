package calc.formula.calculation;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.distr.DistrResultHeader;
import calc.entity.calc.distr.DistrResultLine;
import calc.entity.calc.enums.*;
import calc.entity.calc.source.*;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.exception.CycleDetectionException;
import calc.formula.service.CalcService;
import calc.formula.service.MessageService;
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

import static calc.util.Util.buildMsgParams;
import static calc.util.Util.round;

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
        for (SourceLine2 line : header.getHeader().getLines2()) {
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
            copyTranslates2(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines2(resultLines);
    }

    private void calcLines1(SourceResultHeader header, CalcContext context) {
        List<SourceLine1> lines = header.getHeader().getLines1();

        List<SourceResultLine1> resultLines = new ArrayList<>();
        for (SourceLine1 line : lines) {
            Map<String, String> msgParams = buildMsgParams(line);
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();

            Double val;
            try {
                CalcResult result = calcService.calcMeteringPoint(meteringPoint, param, ParamTypeEnum.PT, context);
                val = result != null ? result.getDoubleValue() : null;
            }
            catch (CycleDetectionException e) {
                messageService.addMessage(header, line.getId(), docCode, "CYCLED_FORMULA", msgParams);
                continue;
            }
            catch (Exception e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, "ERROR_FORMULA", msgParams);
                continue;
            }

            SourceResultLine1 resultLine = new SourceResultLine1();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(meteringPoint);
            resultLine.setParam(param);
            resultLine.setTotalVal(val);
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(LocalDateTime.now());
            copyTranslates1(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines1(resultLines);
    }

    private void calcLines2(SourceResultHeader header, CalcContext context) {
        List<SourceResultLine2> resultLines = new ArrayList<>();
        for (SourceLine2 line : header.getHeader().getLines2()) {
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

            List<DistrResultHeader> distributionList = distrResultHeaderRepo.findByOrg(header.getOrganization().getId(), header.getDataType().name(), header.getStartDate(), header.getEndDate());
            DistrResultLine distributionLine = distributionList.stream()
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

            if (distributionLine != null) {
                resultLine.setOwnVal(distributionLine.getOwnVal());
                resultLine.setOtherVal(distributionLine.getOtherVal());
                resultLine.setTotalVal(distributionLine.getTotalVal());
            }

            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(LocalDateTime.now());
            copyTranslates2(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines2(resultLines);
    }

    private void copyTranslates1(SourceLine1 line, SourceResultLine1 resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (SourceLine1Translate lineTranslate : line.getTranslates()) {
            SourceResultLine1Translate resultLineTranslate = new SourceResultLine1Translate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);

            resultLineTranslate.setName(lineTranslate.getName());
            if (resultLineTranslate == null && resultLine.getMeteringPoint()!=null)
                resultLineTranslate.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    private void copyTranslates2(SourceLine2 line, SourceResultLine2 resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (SourceLine2Translate lineTranslate : line.getTranslates()) {
            SourceResultLine2Translate resultLineTranslate = new SourceResultLine2Translate();
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
        List<SourceResultLine1> lines1 = sourceResultLine1Repo.findAllByHeaderId(header.getId());
        sourceResultLine1Repo.delete(lines1);
        sourceResultLine1Repo.flush();

        List<SourceResultLine2> lines2 = sourceResultLine2Repo.findAllByHeaderId(header.getId());
        sourceResultLine2Repo.delete(lines2);
        sourceResultLine2Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(SourceResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void setParents(SourceResultHeader header) {
        List<SourceResultLine2> resultLines = sourceResultLine2Repo.findAllByHeaderId(header.getId());
        List<SourceLine2> sourceLines = header.getHeader().getLines2();

        for (SourceLine2 sourceLine : sourceLines) {
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
}
