package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.pe.BalanceSubstPeLine;
import calc.entity.calc.bs.pe.PowerTransformerValue;
import calc.entity.calc.enums.TransformerTypeEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.WindingNumber;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextTypeEnum;
import calc.formula.ParamValue;
import calc.formula.exception.CalcServiceException;
import calc.formula.expression.impl.*;
import calc.formula.service.*;
import calc.repo.calc.PowerTransformerValueRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

import static calc.util.Util.buildMsgParams;
import static calc.util.Util.round;
import static java.lang.Math.*;
import static java.util.Optional.*;

@SuppressWarnings("Duplicates")
@Service
@RequiredArgsConstructor
public class BalanceSubstTransformerService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstTransformerService.class);
    private static final String docCode = "LOSSES";
    private final WorkingHoursService workingHoursService;
    private final PowerTransformerService powerTransformerService;
    private final BalanceSubstResultUService resultUService;
    private final CalcService calcService;
    private final MessageService messageService;
    private final ParamService paramService;
    private final PowerTransformerValueRepo powerTransformerValueRepo;

    public boolean calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("Power equipment losses for balance with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.MR)
                .build();

            List<PowerTransformerValue> lines = calcLines(header, context);
            deleteLines(header);
            saveLines(lines);

            logger.info("Transformer losses for balance with headerId " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", e.getClass().getCanonicalName());
            logger.error("Transformer losses for balance with headerId " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<PowerTransformerValue> calcLines(BalanceSubstResultHeader header, CalcContext context) throws Exception  {
        Parameter paramWL = paramService.getParam("WL");
        Unit unitWL = paramWL.getUnit();

        List<PowerTransformerValue> transformerLines = new ArrayList<>();
        for (BalanceSubstPeLine peLine : header.getHeader().getPeLines()) {
            PowerTransformer transformer = peLine.getPowerTransformer();
            if (transformer == null)
                continue;

            String info = "";
            if (transformer.getTranslates().containsKey(context.getLang()))
                info = transformer.getTranslates()
                    .get(context.getLang())
                    .getName();

            if (transformer.getWindingsNumber() == null) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_WN_NOT_FOUND", info);
                continue;
            }

            MeteringPoint inputMpH = transformer.getInputMpH();
            MeteringPoint inputMpM = transformer.getInputMpM();
            MeteringPoint inputMpL = transformer.getInputMpL();
            MeteringPoint inputMp =  transformer.getInputMp();

            if  (transformer.getWindNum() == WindingNumber.THREE && (inputMp == null || inputMpH == null || inputMpM == null || inputMpL == null)) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_INPUT_NOT_FOUND", info);
                continue;
            }

            if  (transformer.getWindNum() == WindingNumber.TWO && (inputMp == null || (inputMpH == null && inputMpL == null))) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_INPUT_NOT_FOUND", info);
                continue;
            }

            Double sNom     = getAttr(transformer, "snom",      context);
            Double uNomH    = getAttr(transformer, "unom_h",    context);
            Double deltaPxx = getAttr(transformer, "delta_pxx", context);
            Double pkzHM    = getAttr(transformer, "pkz_hm",    context);
            Double pkzML    = getAttr(transformer, "pkz_ml",    context);
            Double pkzHL    = getAttr(transformer, "pkz_hl",    context);

            if (transformer.getWindNum() == WindingNumber.THREE && transformer.getTransformerType() == TransformerTypeEnum.AT) {
                Double alpha = 1d;
                if (transformer.getSlNom() != null && transformer.getShNom() != null)
                    alpha = transformer.getSlNom() / transformer.getShNom();

                pkzHL = pkzHL / pow(alpha, 2);
                pkzML = pkzML / pow(alpha, 2);
            }

            Double hours = WorkingHoursExpression.builder()
                .objectType("tr")
                .objectId(transformer.getId())
                .context(context)
                .service(workingHoursService)
                .build()
                .doubleValue();

            hours = round(hours, 1);

            Double uAvg = UavgExpression.builder()
                .meteringPointCode(inputMp.getCode())
                .def(inputMp.getVoltageClass()!=null ? inputMp.getVoltageClass().getValue() / 1000d : 0d)
                .context(context)
                .service(resultUService)
                .build()
                .doubleValue();

            Parameter parU = paramService.getParam("U");
            uAvg = round(uAvg, parU);

            if (sNom  == 0) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_SNOM_NOT_FOUND", info);
                continue;
            }

            if (uNomH == 0) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_UNOMH_NOT_FOUND", info);
                continue;
            }

            if (uAvg  == 0) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_UAVG_NOT_FOUND", info);
                continue;
            }

            PowerTransformerValue transformerLine = new PowerTransformerValue();
            transformerLine.setHeader(header);
            transformerLine.setTransformer(transformer);
            transformerLine.setDeltaPXX(deltaPxx);
            transformerLine.setSnom(sNom);
            transformerLine.setUnomH(uNomH);
            transformerLine.setInputMp(inputMp);
            transformerLine.setInputMpH(inputMpH);
            transformerLine.setInputMpM(inputMpM);
            transformerLine.setInputMpL(inputMpL);
            transformerLine.setPkzHM(pkzHM);
            transformerLine.setPkzML(pkzML);
            transformerLine.setPkzHL(pkzHL);
            transformerLine.setUnit(unitWL);
            transformerLine.setOperatingTime(hours);
            transformerLine.setUavg(uAvg);
            transformerLine.setWindingsNumber(transformer.getWindingsNumber());
            transformerLine.setIsBalance(peLine.getIsBalance());
            transformerLine.setMeteringPointOut(peLine.getMeteringPointOut());
            transformerLine.setIsBalance(peLine.getIsBalance());

            if (transformer.getWindNum() == WindingNumber.TWO) {
                Double rpH = null, rmH = null, apH = null, amH = null, totalAH = null, totalRH = null, totalEH = null;
                if (inputMpH != null) {
                    Map<String, String> msgParams = buildMsgParams(inputMpH);
                    try {
                        apH = getMrVal(inputMpH, paramService.getParam("A+"), context);
                        amH = getMrVal(inputMpH, paramService.getParam("A-"), context);
                        rpH = getMrVal(inputMpH, paramService.getParam("R+"), context);
                        rmH = getMrVal(inputMpH, paramService.getParam("R-"), context);
                    }
                    catch (CalcServiceException e) {
                        msgParams.putIfAbsent("err", e.getMessage());
                        messageService.addMessage(header, peLine.getId(), docCode, e.getErrCode(), msgParams);
                        continue;
                    }

                    totalAH = ofNullable(apH).orElse(0d) + ofNullable(amH).orElse(0d);
                    totalRH = ofNullable(rpH).orElse(0d) + ofNullable(rmH).orElse(0d);
                    totalEH = pow(totalAH, 2) + pow(totalRH, 2);
                }

                Double rpL = null, rmL = null, apL = null, amL = null, totalAL = null, totalRL = null, totalEL = null;
                if (inputMpL != null) {
                    Map<String, String> msgParams = buildMsgParams(inputMpL);
                    try {
                        apL = getMrVal(inputMpL, paramService.getParam("A+"), context);
                        amL = getMrVal(inputMpL, paramService.getParam("A-"), context);
                        rpL = getMrVal(inputMpL, paramService.getParam("R+"), context);
                        rmL = getMrVal(inputMpL, paramService.getParam("R-"), context);
                    }
                    catch (CalcServiceException e) {
                        msgParams.putIfAbsent("err", e.getMessage());
                        messageService.addMessage(header, peLine.getId(), docCode, e.getErrCode(), msgParams);
                        continue;
                    }

                    totalAL = ofNullable(apL).orElse(0d) + ofNullable(amL).orElse(0d);
                    totalRL = ofNullable(rpL).orElse(0d) + ofNullable(rmL).orElse(0d);
                    totalEL = pow(totalAL, 2) + pow(totalRL, 2);
                }

                Double totalE = inputMpH != null ? totalEH : totalEL;

                Double resist = transformer.getResist();
                if (resist == null) {
                    messageService.addMessage(header, peLine.getId(), docCode, "PE_RESIST_NOT_FOUND", info);
                    resist = pkzHL * (pow(uNomH, 2) / pow(sNom, 2)) * 1000d;
                }

                Double valXX = round(deltaPxx * hours * pow(uAvg / uNomH, 2), paramWL);
                Double valN  = round(totalE * resist / (pow(uAvg, 2) * 1000d * hours), paramWL);

                transformerLine.setApH(apH);
                transformerLine.setAmH(amH);
                transformerLine.setRpH(rpH);
                transformerLine.setRmH(rmH);
                transformerLine.setTotalAEH(totalAH);
                transformerLine.setTotalREH(totalRH);
                transformerLine.setTotalEH(totalEH);

                transformerLine.setApL(apL);
                transformerLine.setAmL(amL);
                transformerLine.setRpL(rpL);
                transformerLine.setRmL(rmL);
                transformerLine.setTotalAEL(totalAL);
                transformerLine.setTotalREL(totalRL);
                transformerLine.setTotalEL(totalEL);

                transformerLine.setResistH(resist);
                transformerLine.setValXX(valXX);
                transformerLine.setValN(valN);
            }

            if (transformer.getWindNum() == WindingNumber.THREE) {
                Map<String, String> msgParams = buildMsgParams(inputMpL);
                Double apL = null, amL = null, rpL = null, rmL = null;
                try {
                    amL = getMrVal(inputMpL, paramService.getParam("A-"), context);
                    apL = getMrVal(inputMpL, paramService.getParam("A+"), context);
                    rpL = getMrVal(inputMpL, paramService.getParam("R+"), context);
                    rmL = getMrVal(inputMpL, paramService.getParam("R-"), context);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, peLine.getId(), docCode, e.getErrCode(), msgParams);
                }

                msgParams = buildMsgParams(inputMpM);
                Double apM = null, amM = null, rpM = null, rmM = null;
                try {
                    apM = getMrVal(inputMpM, paramService.getParam("A+"), context);
                    amM = getMrVal(inputMpM, paramService.getParam("A-"), context);
                    rpM = getMrVal(inputMpM, paramService.getParam("R+"), context);
                    rmM = getMrVal(inputMpM, paramService.getParam("R-"), context);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, peLine.getId(), docCode, e.getErrCode(), msgParams);
                }

                msgParams = buildMsgParams(inputMpH);
                Double apH = null, amH = null, rpH = null, rmH = null;
                try {
                    apH = getMrVal(inputMpH, paramService.getParam("A+"), context);
                    amH = getMrVal(inputMpH, paramService.getParam("A-"), context);
                    rpH = getMrVal(inputMpH, paramService.getParam("R+"), context);
                    rmH = getMrVal(inputMpH, paramService.getParam("R-"), context);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, peLine.getId(), docCode, e.getErrCode(), msgParams);
                }

                Double totalAL = ofNullable(apL).orElse(0d) + ofNullable(amL).orElse(0d);
                Double totalRL = ofNullable(rpL).orElse(0d) + ofNullable(rmL).orElse(0d);
                Double totalAM = ofNullable(apM).orElse(0d) + ofNullable(amM).orElse(0d);
                Double totalRM = ofNullable(rpM).orElse(0d) + ofNullable(rmM).orElse(0d);
                Double totalAH = ofNullable(apH).orElse(0d) + ofNullable(amH).orElse(0d);
                Double totalRH = ofNullable(rpH).orElse(0d) + ofNullable(rmH).orElse(0d);

                Double totalEL = pow(totalAL, 2) + pow(totalRL, 2);
                Double totalEM = pow(totalAM, 2) + pow(totalRM, 2);
                Double totalEH = pow(totalAH, 2) + pow(totalRH, 2);

                Double resistL = transformer.getResistL();
                if (resistL == null) {
                    messageService.addMessage(header, peLine.getId(), docCode, "PE_RESIST_NOT_FOUND", info);
                    resistL = (pkzHL + pkzML - pkzHM) / 2d * pow(uNomH / sNom, 2) * 1000d;
                }

                Double resistM = transformer.getResistM();
                if (resistM == null) {
                    messageService.addMessage(header, peLine.getId(), docCode, "PE_RESIST_NOT_FOUND", info);
                    resistM = (pkzHM + pkzML - pkzHL) / 2d * pow(uNomH / sNom, 2) * 1000d;
                }

                Double resistH = transformer.getResistH();
                if (resistH == null) {
                    messageService.addMessage(header, peLine.getId(), docCode, "PE_RESIST_NOT_FOUND", info);
                    resistH = (pkzHM + pkzHL - pkzML) / 2d * pow(uNomH / sNom, 2) * 1000d;
                }

                Double valXX = round(deltaPxx * hours * pow(uAvg / uNomH, 2), paramWL);
                Double valN  = round((totalEL * resistL + totalEM * resistM + totalEH * resistH) / (pow(uAvg,2) * hours * 1000d), paramWL);

                transformerLine.setApL(apL);
                transformerLine.setAmL(amL);
                transformerLine.setRpL(rpL);
                transformerLine.setRmL(rmL);
                transformerLine.setApM(apM);
                transformerLine.setAmM(amM);
                transformerLine.setRpM(rpM);
                transformerLine.setRmM(rmM);
                transformerLine.setApH(apH);
                transformerLine.setAmH(amH);
                transformerLine.setRpH(rpH);
                transformerLine.setRmH(rmH);
                transformerLine.setTotalAEH(totalAH);
                transformerLine.setTotalREH(totalRH);
                transformerLine.setTotalEH(totalEH);
                transformerLine.setTotalAEM(totalAM);
                transformerLine.setTotalREM(totalRM);
                transformerLine.setTotalEM(totalEM);
                transformerLine.setTotalAEL(totalAL);
                transformerLine.setTotalREL(totalRL);
                transformerLine.setTotalEL(totalEL);
                transformerLine.setResistH(resistH);
                transformerLine.setResistM(resistM);
                transformerLine.setResistL(resistL);
                transformerLine.setValXX(valXX);
                transformerLine.setValN(valN);
            }
            transformerLine.setVal(transformerLine.getValXX() + transformerLine.getValN());

            transformerLines.add(transformerLine);
            if (transformerLine.getMeteringPointOut() != null)
                context.getTransformerValues().putIfAbsent(transformerLine.getMeteringPointOut().getCode(), transformerLine.getVal());
        }
        return transformerLines;
    }

    private Double getAttr(PowerTransformer transformer, String attr, CalcContext context) {
        return PowerTransformerExpression.builder()
            .id(transformer.getId())
            .attr(attr)
            .def(0d)
            .context(context)
            .service(powerTransformerService)
            .build()
            .doubleValue();
    }

    private ParamValue getMrVal(MeteringPoint meteringPoint, CalcContext context) {
        Double ap = getMrVal(meteringPoint, paramService.getParam("A+"), context);
        Double am = getMrVal(meteringPoint, paramService.getParam("A-"), context);
        Double rp = getMrVal(meteringPoint, paramService.getParam("R+"), context);
        Double rm = getMrVal(meteringPoint, paramService.getParam("R-"), context);
        return new ParamValue(ap, am, rp, rm);
    }

    private Double getMrVal(MeteringPoint meteringPoint, Parameter param, CalcContext context) {
        if (meteringPoint == null || param == null)
            return null;

        CalcResult result = calcService.calcValue(meteringPoint, param, context);
        return result != null ? result.getDoubleValue() : null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<PowerTransformerValue> transformerLines) {
        powerTransformerValueRepo.save(transformerLines);
        powerTransformerValueRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(BalanceSubstResultHeader header) {
        List<PowerTransformerValue> transformerValues = powerTransformerValueRepo.findAllByHeaderId(header.getId());
        powerTransformerValueRepo.delete(transformerValues);
        powerTransformerValueRepo.flush();
    }
}
