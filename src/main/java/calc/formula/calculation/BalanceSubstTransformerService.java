package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.BalanceSubstResultHeader;
import calc.entity.calc.bs.pe.BalanceSubstPeLine;
import calc.entity.calc.bs.pe.PowerTransformerValue;
import calc.entity.calc.enums.TransformerTypeEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextType;
import calc.formula.exception.CycleDetectionException;
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
                .docCode(docCode)
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .defContextType(ContextType.MR)
                .values(new HashMap<>())
                .transformerValues(new HashMap<>())
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
        Parameter paramWL = paramService.getValues().get("WL");
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

            if  (transformer.getWindingsNumber().equals(3l) && (inputMp == null || inputMpH == null || inputMpM == null || inputMpL == null)) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_INPUT_NOT_FOUND", info);
                continue;
            }

            if  (transformer.getWindingsNumber().equals(2l) && (inputMp == null || (inputMpH == null && inputMpL == null))) {
                messageService.addMessage(header, peLine.getId(), docCode, "PE_INPUT_NOT_FOUND", info);
                continue;
            }

            Double sNom     = getTransformerAttr(transformer, "snom",      context);
            Double uNomH    = getTransformerAttr(transformer, "unom_h",    context);
            Double deltaPxx = getTransformerAttr(transformer, "delta_pxx", context);
            Double pkzHM    = getTransformerAttr(transformer, "pkz_hm",    context);
            Double pkzML    = getTransformerAttr(transformer, "pkz_ml",    context);
            Double pkzHL    = getTransformerAttr(transformer, "pkz_hl",    context);

            if (transformer.getWindingsNumber().equals(3l) && transformer.getTransformerType() == TransformerTypeEnum.AT) {
                Double alpha = 1d;
                if (transformer.getSlNom() != null && transformer.getShNom() != null)
                    alpha = transformer.getSlNom() / transformer.getShNom();

                pkzHL = pkzHL / Math.pow(alpha, 2);
                pkzML = pkzML / Math.pow(alpha, 2);
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

            Parameter parU = paramService.getValues().get("U");
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

            if (transformer.getWindingsNumber().equals(2l)) {
                Double rpH = null, rmH = null, apH = null, amH = null, totalAH = null, totalRH = null, totalEH = null;
                if (inputMpH != null) {
                    Map<String, String> msgParams = buildMsgParams(inputMpH);
                    try {
                        apH = getMrVal(inputMpH, paramService.getValues().get("A+"), context);
                        amH = getMrVal(inputMpH, paramService.getValues().get("A-"), context);
                        rpH = getMrVal(inputMpH, paramService.getValues().get("R+"), context);
                        rmH = getMrVal(inputMpH, paramService.getValues().get("R-"), context);
                    }
                    catch (CycleDetectionException e) {
                        messageService.addMessage(header, peLine.getId(), docCode, "CYCLED_FORMULA", msgParams);
                        continue;
                    }
                    catch (Exception e) {
                        msgParams.putIfAbsent("err", e.getMessage());
                        messageService.addMessage(header, peLine.getId(), docCode, "ERROR_FORMULA", msgParams);
                        continue;
                    }

                    totalAH = Optional.ofNullable(apH).orElse(0d) + Optional.ofNullable(amH).orElse(0d);
                    totalRH = Optional.ofNullable(rpH).orElse(0d) + Optional.ofNullable(rmH).orElse(0d);
                    totalEH = Math.pow(totalAH, 2) + Math.pow(totalRH, 2);
                }

                Double rpL = null, rmL = null, apL = null, amL = null, totalAL = null, totalRL = null, totalEL = null;
                if (inputMpL != null) {
                    Map<String, String> msgParams = buildMsgParams(inputMpL);
                    try {
                        apL = getMrVal(inputMpL, paramService.getValues().get("A+"), context);
                        amL = getMrVal(inputMpL, paramService.getValues().get("A-"), context);
                        rpL = getMrVal(inputMpL, paramService.getValues().get("R+"), context);
                        rmL = getMrVal(inputMpL, paramService.getValues().get("R-"), context);
                    }
                    catch (CycleDetectionException e) {
                        messageService.addMessage(header, peLine.getId(), docCode, "CYCLED_FORMULA", msgParams);
                        continue;
                    }
                    catch (Exception e) {
                        msgParams.putIfAbsent("err", e.getMessage());
                        messageService.addMessage(header, peLine.getId(), docCode, "ERROR_FORMULA", msgParams);
                        continue;
                    }

                    totalAL = Optional.ofNullable(apL).orElse(0d) + Optional.ofNullable(amL).orElse(0d);
                    totalRL = Optional.ofNullable(rpL).orElse(0d) + Optional.ofNullable(rmL).orElse(0d);
                    totalEL = Math.pow(totalAL, 2) + Math.pow(totalRL, 2);
                }

                Double totalE = inputMpH != null ? totalEH : totalEL;
                Double resist = pkzHL * (Math.pow(uNomH, 2) / Math.pow(sNom, 2)) * 1000d;
                Double valXX = round(deltaPxx * hours * Math.pow(uAvg / uNomH, 2), paramWL);
                Double valN  = round(totalE * resist / (Math.pow(uAvg, 2) * 1000d * hours), paramWL);

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

            if (transformer.getWindingsNumber().equals(3l)) {
                Map<String, String> msgParams = buildMsgParams(inputMpL);
                Double apL; Double amL; Double rpL; Double rmL;
                try {
                    amL = getMrVal(inputMpL, paramService.getValues().get("A-"), context);
                    apL = getMrVal(inputMpL, paramService.getValues().get("A+"), context);
                    rpL = getMrVal(inputMpL, paramService.getValues().get("R+"), context);
                    rmL = getMrVal(inputMpL, paramService.getValues().get("R-"), context);
                }
                catch (CycleDetectionException e) {
                    messageService.addMessage(header, peLine.getId(), docCode, "CYCLED_FORMULA", msgParams);
                    continue;
                }
                catch (Exception e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, peLine.getId(), docCode, "ERROR_FORMULA", msgParams);
                    continue;
                }

                msgParams = buildMsgParams(inputMpM);
                Double apM; Double amM; Double rpM; Double rmM;
                try {
                    apM = getMrVal(inputMpM, paramService.getValues().get("A+"), context);
                    amM = getMrVal(inputMpM, paramService.getValues().get("A-"), context);
                    rpM = getMrVal(inputMpM, paramService.getValues().get("R+"), context);
                    rmM = getMrVal(inputMpM, paramService.getValues().get("R-"), context);
                }
                catch (CycleDetectionException e) {
                    messageService.addMessage(header, peLine.getId(), docCode, "CYCLED_FORMULA", msgParams);
                    continue;
                }
                catch (Exception e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, peLine.getId(), docCode, "ERROR_FORMULA", msgParams);
                    continue;
                }

                msgParams = buildMsgParams(inputMpH);
                Double apH; Double amH; Double rpH; Double rmH;
                try {
                    apH = getMrVal(inputMpH, paramService.getValues().get("A+"), context);
                    amH = getMrVal(inputMpH, paramService.getValues().get("A-"), context);
                    rpH = getMrVal(inputMpH, paramService.getValues().get("R+"), context);
                    rmH = getMrVal(inputMpH, paramService.getValues().get("R-"), context);
                }
                catch (CycleDetectionException e) {
                    messageService.addMessage(header, peLine.getId(), docCode, "CYCLED_FORMULA", msgParams);
                    continue;
                }
                catch (Exception e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, peLine.getId(), docCode, "ERROR_FORMULA", msgParams);
                    continue;
                }

                Double totalAL = Optional.ofNullable(apL).orElse(0d) + Optional.ofNullable(amL).orElse(0d);
                Double totalRL = Optional.ofNullable(rpL).orElse(0d) + Optional.ofNullable(rmL).orElse(0d);
                Double totalAM = Optional.ofNullable(apM).orElse(0d) + Optional.ofNullable(amM).orElse(0d);
                Double totalRM = Optional.ofNullable(rpM).orElse(0d) + Optional.ofNullable(rmM).orElse(0d);
                Double totalAH = Optional.ofNullable(apH).orElse(0d) + Optional.ofNullable(amH).orElse(0d);
                Double totalRH = Optional.ofNullable(rpH).orElse(0d) + Optional.ofNullable(rmH).orElse(0d);

                Double totalEL = Math.pow(totalAL, 2) + Math.pow(totalRL, 2);
                Double totalEM = Math.pow(totalAM, 2) + Math.pow(totalRM, 2);
                Double totalEH = Math.pow(totalAH, 2) + Math.pow(totalRH, 2);

                Double resistL = (pkzHL + pkzML - pkzHM) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;
                Double resistM = (pkzHM + pkzML - pkzHL) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;
                Double resistH = (pkzHM + pkzHL - pkzML) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;

                Double valXX = round(deltaPxx * hours * Math.pow(uAvg / uNomH, 2), paramWL);
                Double valN  = round((totalEL * resistL + totalEM * resistM + totalEH * resistH) / (Math.pow(uAvg,2) * hours * 1000d), paramWL);

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

    private Double getTransformerAttr(PowerTransformer transformer, String attr, CalcContext context) {
        return PowerTransformerExpression.builder()
            .id(transformer.getId())
            .attr(attr)
            .def(0d)
            .context(context)
            .service(powerTransformerService)
            .build()
            .doubleValue();
    }

    private Double getMrVal(MeteringPoint meteringPoint, Parameter param, CalcContext context) throws Exception {
        if (meteringPoint == null)
            return null;

        CalcResult result = calcService.calcMeteringPoint(meteringPoint, param, ParamTypeEnum.PT, context);
        Double value = result!=null ? result.getDoubleValue() : null;
        if (context.getException() != null)
            throw context.getException();

        if (value !=null && Double.isNaN(value))
            return null;

        return value;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<PowerTransformerValue> transformerLines) {
        powerTransformerValueRepo.save(transformerLines);
        powerTransformerValueRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(BalanceSubstResultHeader header) {
        List<PowerTransformerValue> transformerValues = powerTransformerValueRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<transformerValues.size(); i++)
            powerTransformerValueRepo.delete(transformerValues.get(i));
        powerTransformerValueRepo.flush();
    }
}
