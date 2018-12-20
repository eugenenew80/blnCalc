package calc.formula.calculation;

import calc.entity.calc.*;;
import calc.entity.calc.enums.*;
import calc.entity.calc.source.*;
import calc.formula.*;
import calc.formula.exception.*;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import static calc.util.Util.*;
import static java.util.Optional.*;

@SuppressWarnings({"ImplicitSubclassInspection"})
@Service
@RequiredArgsConstructor
public class SourceService {
    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);
    private static final String docCode = "SOURCE";
    private final SourceResultHeaderRepo sourceResultHeaderRepo;
    private final SourceResultLine1Repo sourceResultLine1Repo;
    private final SourceResultLine2Repo sourceResultLine2Repo;
    private final MessageService messageService;
    private final CalcService calcService;

    public boolean calc(Long headerId) {
        logger.info("Energy source balance for header " + headerId + " started");
        SourceResultHeader header = sourceResultHeaderRepo.findOne(headerId);
        if (header.getStatus() != BatchStatusEnum.W)
            return false;

        if (header.getDataType() == null)
            header.setDataType(header.getPeriodType() == PeriodTypeEnum.M ? DataTypeEnum.FINAL : DataTypeEnum.OPER);

        CalcContext context = CalcContext.builder()
            .lang(LangEnum.RU)
            .header(header)
            .build();

        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header);

            CalcResult result = calcService.calcValue(header.getFormula(), context);
            Double value = result !=null ? result.getDoubleValue() : null;
            value = round(value, 0);
            header.setDeliveryVal(value);

            calcLines1(header, context);
            calcLines2(header, context);

            header.setLastUpdateDate(LocalDateTime.now());

            updateStatus(header, BatchStatusEnum.C);
            logger.info("Energy source balance for header " + header.getId() + " completed");

            return true;
        }
        catch (Exception e) {
            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", buildMsgParams(e));
            e.printStackTrace();

            updateStatus(header, BatchStatusEnum.E);
            logger.error("Energy source balance for header " + header.getId() + " terminated with exception: " + e.getMessage());
            return false;
        }
    }

    private void calcLines1(SourceResultHeader header, CalcContext context) {
        List<SourceResultLine1> results = new ArrayList<>();
        for (SourceLine1 line : header.getHeader().getLines1()) {
            Map<String, String> msgParams = buildMsgParams(line);
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();

            Double val = null;
            try {
                CalcResult calc = calcService.calcValue(meteringPoint, param, context);
                val = calc != null ? calc.getDoubleValue() : null;
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
            }

            SourceResultLine1 result = new SourceResultLine1();
            result.setHeader(header);
            result.setLineNum(line.getLineNum());
            result.setMeteringPoint(meteringPoint);
            result.setParam(param);
            result.setTotalVal(val);
            result.setCreateBy(header.getCreateBy());
            result.setCreateDate(LocalDateTime.now());
            copyTranslates1(line, result);
            results.add(result);
        }
        saveLines1(results);
    }

    private void calcLines2(SourceResultHeader header, CalcContext context) {
        List<SourceResultLine2> results = new ArrayList<>();
        for (SourceLine2 line : header.getHeader().getLines2()) {
            Map<String, String> msgParams = buildMsgParams(line);
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Parameter param = line.getParam();

            SourceResultLine2 result = new SourceResultLine2();
            copyTranslates2(line, result);
            results.add(result);

            result.setHeader(header);
            result.setLineNum(line.getLineNum());
            result.setMeteringPoint(line.getMeteringPoint());
            result.setParam(line.getParam());
            result.setIsInverse(line.getIsInverse());
            result.setRowType(line.getRowType());
            result.setCreateBy(header.getCreateBy());
            result.setCreateDate(LocalDateTime.now());

            if (line.getRowType() == RowTypeEnum.GROUP)
                continue;

            CalcProperty.CalcPropertyBuilder pb = CalcProperty.builder()
                .determiningMethod(DeterminingMethodEnum.RDV);

            Double ownVal = null;
            try {
                CalcResult calc = calcService.calcValue(meteringPoint, param, context, pb.gridType(GridTypeEnum.OWN).build());
                ownVal = calc != null ? calc.getDoubleValue() : null;
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getLineNum(), docCode, e.getErrCode(), msgParams);
            }

            Double otherVal = null;
            try {
                CalcResult calc = calcService.calcValue(meteringPoint, param, context, pb.gridType(GridTypeEnum.OTHER).build());
                otherVal = calc != null ? calc.getDoubleValue() : null;
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getLineNum(), docCode, e.getErrCode(), msgParams);
            }

            Double totalVal = null;
            try {
                CalcResult calc = calcService.calcValue(meteringPoint, param, context, pb.gridType(GridTypeEnum.TOTAL).build());
                totalVal = calc != null ? calc.getDoubleValue() : null;
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
            }

            result.setOwnVal(ownVal);
            result.setOtherVal(otherVal);
            result.setTotalVal(totalVal);
        }
        saveLines2(results);
        setParents2(header);
    }

    private void copyTranslates1(SourceLine1 line, SourceResultLine1 resultLine) {
        resultLine.setTranslates(ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (SourceLine1Translate ltl : line.getTranslates()) {
            SourceResultLine1Translate rtl = new SourceResultLine1Translate();
            rtl.setLang(ltl.getLang());
            rtl.setLine(resultLine);
            rtl.setName(ltl.getName());

            if (rtl.getName() == null && resultLine.getMeteringPoint()!=null)
                rtl.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(rtl);
        }
    }

    private void copyTranslates2(SourceLine2 line, SourceResultLine2 resultLine) {
        resultLine.setTranslates(ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (SourceLine2Translate ltl : line.getTranslates()) {
            SourceResultLine2Translate rtl = new SourceResultLine2Translate();
            rtl.setLang(ltl.getLang());
            rtl.setLine(resultLine);
            rtl.setName(ltl.getName());

            if (rtl.getName() == null && resultLine.getMeteringPoint()!=null)
                rtl.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(rtl);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void saveLines1(List<SourceResultLine1> lines) {
        sourceResultLine1Repo.save(lines);
        sourceResultLine1Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void saveLines2(List<SourceResultLine2> lines) {
        sourceResultLine2Repo.save(lines);
        sourceResultLine2Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void deleteLines(SourceResultHeader header) {
        List<SourceResultLine1> lines1 = sourceResultLine1Repo.findAllByHeaderId(header.getId());
        sourceResultLine1Repo.delete(lines1);
        sourceResultLine1Repo.flush();

        List<SourceResultLine2> lines2 = sourceResultLine2Repo.findAllByHeaderId(header.getId());
        sourceResultLine2Repo.delete(lines2);
        sourceResultLine2Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    public void deleteMessages(SourceResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void setParents2(SourceResultHeader header) {
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void updateStatus(SourceResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        sourceResultHeaderRepo.save(header);
        sourceResultHeaderRepo.flush();
    }
}
