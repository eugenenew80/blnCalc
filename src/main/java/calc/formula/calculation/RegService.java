package calc.formula.calculation;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.Parameter;
import calc.entity.calc.enums.*;
import calc.entity.calc.reg.*;
import calc.formula.CalcContext;
import calc.formula.CalcProperty;
import calc.formula.CalcResult;
import calc.formula.exception.CalcServiceException;
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
import static calc.util.Util.inverseParam;
import static java.util.Optional.*;

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

            calcLines1(header, context);
            calcLines2(header, context);
            calcLines3(header, context);
            setParents3(header);
            calcLines4(header, context);

            header.setLastUpdateDate(LocalDateTime.now());

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

            Double ap = null;
            try {
                Parameter param = line.getIsInverse() ? paramService.getParam("A-") : paramService.getParam("A+");
                CalcResult result = calcService.calcValue(meteringPoint, param, context);
                ap = result != null ? result.getDoubleValue() : null;
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
            }

            Double am = null;
            try {
                Parameter param = line.getIsInverse() ?  paramService.getParam("A+") : paramService.getParam("A-");
                CalcResult result = calcService.calcValue(meteringPoint, param, context);
                am = result != null ? result.getDoubleValue() : null;
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
            }

            RegResultLine1 resultLine = new RegResultLine1();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setAp(ap);
            resultLine.setAm(am);
            resultLine.setBalance(ofNullable(ap).orElse(0d) - ofNullable(am).orElse(0d));
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

            Double val = null;
            try {
                CalcResult result = calcService.calcValue(meteringPoint, inverseParam(paramService, param, line.getIsInverse()), context);
                val = result != null ? result.getDoubleValue() : null;
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
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

            RegResultLine3 resultLine = new RegResultLine3();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setParam(line.getParam());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setBalanceUnit(line.getBalanceUnit());
            resultLine.setOwnVal(null);
            resultLine.setOtherVal(null);
            resultLine.setTotalVal(null);
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(header.getCreateDate());

            for (RegLine3Det detail : line.getDetails()) {
                Double ownVal = null;
                try {
                    CalcProperty property = CalcProperty.builder()
                        .determiningMethod(DeterminingMethodEnum.RDV)
                        .gridType(GridTypeEnum.OWN)
                        .electricityGroup(detail.getElectricityGroup())
                        .build();

                    CalcResult result = calcService.calcValue(meteringPoint, param, context, property);
                    ownVal = result != null ? result.getDoubleValue() : null;
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                }

                Double otherVal = null;
                try {
                    CalcProperty property = CalcProperty.builder()
                        .determiningMethod(DeterminingMethodEnum.RDV)
                        .gridType(GridTypeEnum.OTHER)
                        .electricityGroup(detail.getElectricityGroup())
                        .build();

                    CalcResult result = calcService.calcValue(meteringPoint, param, context, property);
                    otherVal = result != null ? result.getDoubleValue() : null;
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                }

                Double totalVal = null;
                try {
                    CalcProperty property = CalcProperty.builder()
                        .determiningMethod(DeterminingMethodEnum.RDV)
                        .gridType(GridTypeEnum.TOTAL)
                        .electricityGroup(detail.getElectricityGroup())
                        .build();

                    CalcResult result = calcService.calcValue(meteringPoint, param, context, property);
                    totalVal = result != null ? result.getDoubleValue() : null;
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                }

                RegResultLine3Det resultLineDet = new RegResultLine3Det();
                resultLineDet.setHeader(header);
                resultLineDet.setLine(resultLine);
                resultLineDet.setElectricityGroup(detail.getElectricityGroup());
                resultLineDet.setOwnVal(ownVal);
                resultLineDet.setOtherVal(otherVal);
                resultLineDet.setTotalVal(totalVal);
                resultLineDet.setCreateBy(header.getCreateBy());
                resultLineDet.setCreateDate(header.getCreateDate());

                if (resultLine.getDetails() == null)
                    resultLine.setDetails(new ArrayList<>());

                resultLine.getDetails().add(resultLineDet);
            }

            copyTranslates3(line, resultLine);
            resultLines.add(resultLine);
        }
        saveLines3(resultLines);
    }

    private void calcLines4(RegResultHeader header, CalcContext context) {
        List<RegResultLine4> resultLines = new ArrayList<>();
        for (RegLine4 line : header.getHeader().getLines4()) {
            MeteringPoint meteringPoint = line.getMeteringPoint();
            if (meteringPoint == null)
                continue;

            Parameter param = paramService.getParam("A+");
            if (param == null)
                continue;

            Map<String, String> msgParams = buildMsgParams(line);

            Double val = null;
            try {
                CalcResult result = calcService.calcValue(meteringPoint, param, context);
                val = result != null ? result.getDoubleValue() : null;
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                continue;
            }

            RegResultLine4 resultLine = new RegResultLine4();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setDealType(line.getDealType());
            resultLine.setDealer(line.getDealer());
            resultLine.setVal(val);
            resultLine.setCreateBy(header.getCreateBy());
            resultLine.setCreateDate(header.getCreateDate());
            resultLines.add(resultLine);
        }
        saveLines4(resultLines);
    }


    private void copyTranslates1(RegLine1 line, RegResultLine1 resultLine) {
        resultLine.setTranslates(ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
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
        resultLine.setTranslates(ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
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
        resultLine.setTranslates(ofNullable(resultLine.getTranslates()).orElse(new ArrayList<>()));
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void setParents3(RegResultHeader header) {
        List<RegResultLine3> resultLines = regResultLine3Repo.findAllByHeaderId(header.getId());
        List<RegLine3> regLines = header.getHeader().getLines3();

        for (RegLine3 regLine : regLines) {
            if (regLine.getParent() == null)
                continue;

            RegResultLine3 line = resultLines.stream()
                .filter(t -> t.getLineNum().equals(regLine.getLineNum()))
                .findFirst()
                .orElse(null);

            RegResultLine3 parentLine = resultLines.stream()
                .filter(t -> t.getLineNum().equals(regLine.getParent().getLineNum()))
                .findFirst()
                .orElse(null);

            if (line != null && parentLine!= null)
                line.setParent(parentLine);
        }

        regResultLine3Repo.save(resultLines);
        regResultLine3Repo.flush();
    }
}
