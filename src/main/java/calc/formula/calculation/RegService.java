package calc.formula.calculation;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.reg.*;
import calc.entity.calc.source.*;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextType;
import calc.formula.exception.CycleDetectionException;
import calc.formula.service.CalcService;
import calc.formula.service.MessageService;
import calc.formula.service.ParamService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static calc.util.Util.buildMsgParams;

@SuppressWarnings({"Duplicates", "ImplicitSubclassInspection"})
@Service
@RequiredArgsConstructor
public class RegService {
    private static final Logger logger = LoggerFactory.getLogger(RegService.class);
    private static final String docCode = "REG";
    private final RegResultHeaderRepo regResultHeaderRepo;
    private final RegResultLine1Repo regResultLine1Repo;
    private final RegResultLine2Repo regResultLine2Repo;
    private final RegResultLine3Repo regResultLine3Repo;
    private final RegResultLine4Repo regResultLine4Repo;
    private final MessageService messageService;
    private final ParamService paramService;
    private final CalcService calcService;

    public boolean calc(Long headerId) {
        logger.info("Reg for header " + headerId + " started");
        RegResultHeader header = regResultHeaderRepo.findOne(headerId);
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

            calcLines1(header, context);
            calcLines2(header, context);
            calcLines3(header, context);
            calcLines4(header, context);

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);

            updateStatus(header, BatchStatusEnum.C);
            logger.info("Reg for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", new HashMap<>());
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Reg for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    private void calcLines1(RegResultHeader header, CalcContext context) {
        List<RegResultLine1> resultLines = new ArrayList<>();
        for (RegLine1 line : header.getHeader().getLines1()) {
            MeteringPoint meteringPoint = line.getMeteringPoint();
            if (meteringPoint == null)
                continue;

            Map<String, String> msgParams = buildMsgParams(line);

            Double ap;
            try {
                Parameter param = line.getIsInverse() ? paramService.getValues().get("A-") : paramService.getValues().get("A+");
                CalcResult result = calcService.calcMeteringPoint(meteringPoint, param, context);
                ap = result != null ? result.getDoubleValue() : null;
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

            Double am;
            try {
                Parameter param = line.getIsInverse() ?  paramService.getValues().get("A+") : paramService.getValues().get("A-");
                CalcResult result = calcService.calcMeteringPoint(meteringPoint, param, context);
                am = result != null ? result.getDoubleValue() : null;
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

            RegResultLine1 resultLine = new RegResultLine1();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setAp(ap);
            resultLine.setAm(am);
            resultLine.setBalance(Optional.of(ap).orElse(0d) - Optional.of(am).orElse(0d));
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(header.getCreateDate());

            copyTranslates1(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines1(resultLines);
    }

    private void calcLines2(RegResultHeader header, CalcContext context) {
        List<RegResultLine2> resultLines = new ArrayList<>();
        for (RegLine2 line : header.getHeader().getLines2()) {
            MeteringPoint meteringPoint = line.getMeteringPoint();
            if (meteringPoint == null)
                continue;

            Parameter param = line.getParam();
            if (param == null)
                continue;

            Map<String, String> msgParams = buildMsgParams(line);

            Double val;
            try {
                CalcResult result = calcService.calcMeteringPoint(meteringPoint, inverseParam(param, line.getIsInverse()), context);
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

            RegResultLine2 resultLine = new RegResultLine2();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setParam(line.getParam());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setVal(val);
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(header.getCreateDate());

            copyTranslates2(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines2(resultLines);
    }

    private void calcLines3(RegResultHeader header, CalcContext context) {
        List<RegResultLine3> resultLines = new ArrayList<>();
        for (RegLine3 line : header.getHeader().getLines3()) {

            MeteringPoint meteringPoint = line.getMeteringPoint();
            if (meteringPoint == null)
                continue;

            Parameter param = line.getParam();
            if (param == null)
                continue;

            Map<String, String> msgParams = buildMsgParams(line);

            Double totalVal;
            try {
                CalcResult result = calcService.calcMeteringPoint(meteringPoint, inverseParam(param, line.getIsInverse()), context);
                totalVal = result != null ? result.getDoubleValue() : null;
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

            RegResultLine3 resultLine = new RegResultLine3();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setParam(line.getParam());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setOwnVal(null);
            resultLine.setOtherVal(null);
            resultLine.setTotalVal(totalVal);
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(header.getCreateDate());

            copyTranslates3(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines3(resultLines);
    }

    private void calcLines4(RegResultHeader header, CalcContext context) {
        List<RegResultLine4> resultLines = new ArrayList<>();
        for (RegLine4 line : header.getHeader().getLines4()) {
            RegResultLine4 resultLine = new RegResultLine4();

            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setDealType(line.getDealType());
            resultLine.setDealer(line.getDealer());
            resultLine.setVal(null);
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(header.getCreateDate());

            resultLines.add(resultLine);
        }
        saveLines4(resultLines);
    }


    private void copyTranslates1(RegLine1 line, RegResultLine1 resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (RegLine1Translate lineTranslate : line.getTranslates()) {
            RegResultLine1Translate resultLineTranslate = new RegResultLine1Translate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);

            resultLineTranslate.setName(lineTranslate.getName());
            if (resultLineTranslate == null && resultLine.getMeteringPoint()!=null)
                resultLineTranslate.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    private void copyTranslates2(RegLine2 line, RegResultLine2 resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (RegLine2Translate lineTranslate : line.getTranslates()) {
            RegResultLine2Translate resultLineTranslate = new RegResultLine2Translate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);

            resultLineTranslate.setName(lineTranslate.getName());
            if (resultLineTranslate == null && resultLine.getMeteringPoint()!=null)
                resultLineTranslate.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    private void copyTranslates3(RegLine3 line, RegResultLine3 resultLine) {
        resultLine.setTranslates(Optional.ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
        for (RegLine3Translate lineTranslate : line.getTranslates()) {
            RegResultLine3Translate resultLineTranslate = new RegResultLine3Translate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);

            resultLineTranslate.setName(lineTranslate.getName());
            if (resultLineTranslate == null && resultLine.getMeteringPoint()!=null)
                resultLineTranslate.setName(resultLine.getMeteringPoint().getShortName());

            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines1(List<RegResultLine1> lines) {
        regResultLine1Repo.save(lines);
        regResultLine1Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines2(List<RegResultLine2> lines) {
        regResultLine2Repo.save(lines);
        regResultLine2Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines3(List<RegResultLine3> lines) {
        regResultLine3Repo.save(lines);
        regResultLine3Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines4(List<RegResultLine4> lines) {
        regResultLine4Repo.save(lines);
        regResultLine4Repo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void deleteLines(RegResultHeader header) {
        List<RegResultLine1> lines1 = regResultLine1Repo.findAllByHeaderId(header.getId());
        regResultLine1Repo.delete(lines1);
        regResultLine1Repo.flush();

        List<RegResultLine2> lines2 = regResultLine2Repo.findAllByHeaderId(header.getId());
        regResultLine2Repo.delete(lines2);
        regResultLine2Repo.flush();

        List<RegResultLine3> lines3 = regResultLine3Repo.findAllByHeaderId(header.getId());
        regResultLine3Repo.delete(lines3);
        regResultLine3Repo.flush();

        List<RegResultLine4> lines4 = regResultLine4Repo.findAllByHeaderId(header.getId());
        regResultLine4Repo.delete(lines4);
        regResultLine4Repo.flush();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(RegResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateStatus(RegResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        regResultHeaderRepo.save(header);
        regResultHeaderRepo.flush();
    }


    private Parameter inverseParam(Parameter param, Boolean isInverse) {
        if (isInverse) {
            if (param.equals(paramService.getValues().get("A+"))) return paramService.getValues().get("A-");
            if (param.equals(paramService.getValues().get("A-"))) return paramService.getValues().get("A+");
            if (param.equals(paramService.getValues().get("R+"))) return paramService.getValues().get("R-");
            if (param.equals(paramService.getValues().get("R-"))) return paramService.getValues().get("R+");
        }
        return param;
    }
}
