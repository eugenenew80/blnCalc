package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.pe.*;
import calc.formula.*;
import calc.entity.calc.bs.*;
import calc.entity.calc.enums.*;
import calc.formula.exception.*;
import calc.formula.expression.impl.*;
import calc.formula.service.*;
import calc.repo.calc.PowerTransformerValueRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static calc.util.Util.buildMsgParams;
import static calc.util.Util.round;
import static java.lang.Math.*;

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

    private List<PowerTransformerValue> calcLines(BalanceSubstResultHeader header, CalcContext context)  {
        List<PowerTransformerValue> results = new ArrayList<>();
        for (BalanceSubstPeLine line : header.getHeader().getPeLines()) {
            PowerTransformer transformer = line.getPowerTransformer();
            if (transformer == null)
                continue;

            Parameter paramWL = paramService.getParam("WL");
            Parameter parU = paramService.getParam("U");
            Unit unitWL = paramWL.getUnit();

            String transformerName = "";
            if (transformer.getTranslates().containsKey(context.getLang()))
                transformerName = transformer.getTranslates()
                    .get(context.getLang())
                    .getName();

            Map<String, String> defMsgParams = buildMsgParams(line);
            defMsgParams.putIfAbsent("pe", transformerName);

            MeteringPoint inputMp =  transformer.getInputMp();
            MeteringPoint inputMpH = transformer.getInputMpH();
            MeteringPoint inputMpM = transformer.getInputMpM();
            MeteringPoint inputMpL = transformer.getInputMpL();

            Double sNom     = getAttr(transformer, "snom",      context);
            Double uNomH    = getAttr(transformer, "unom_h",    context);
            Double deltaPxx = getAttr(transformer, "delta_pxx", context);
            Double pkzHM    = getAttr(transformer, "pkz_hm",    context);
            Double pkzML    = getAttr(transformer, "pkz_ml",    context);
            Double pkzHL    = getAttr(transformer, "pkz_hl",    context);

            if (transformer.getWindNum() == WindingNumber.THREE && transformer.getTransformerType() == TransformerTypeEnum.AT) {
                Double alpha = 1d;
                if (transformer.getSlNom() != null && transformer.getShNom() != null && transformer.getShNom() != 0)
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


            Double uAvgDef = inputMp != null && inputMp.getVoltageClass() != null
                ? inputMp.getVoltageClass().getValue() / 1000d
                : 0d;

            Double uAvg = UavgExpression.builder()
                .meteringPointCode(inputMp.getCode())
                .def(uAvgDef)
                .context(context)
                .service(resultUService)
                .build()
                .doubleValue();

            uAvg = round(uAvg, parU);

            PowerTransformerValue result = new PowerTransformerValue();
            results.add(result);
            result.setHeader(header);
            result.setTransformer(transformer);
            result.setDeltaPXX(deltaPxx);
            result.setSnom(sNom);
            result.setUnomH(uNomH);
            result.setInputMp(inputMp);
            result.setInputMpH(inputMpH);
            result.setInputMpM(inputMpM);
            result.setInputMpL(inputMpL);
            result.setPkzHM(pkzHM);
            result.setPkzML(pkzML);
            result.setPkzHL(pkzHL);
            result.setUnit(unitWL);
            result.setOperatingTime(hours);
            result.setUavg(uAvg);
            result.setWindingsNumber(transformer.getWindingsNumber());
            result.setIsBalance(line.getIsBalance());
            result.setMeteringPointOut(line.getMeteringPointOut());
            result.setIsBalance(line.getIsBalance());
            result.setIsLosses(line.getIsLosses());


            if (transformer.getWindingsNumber() == null) {
                messageService.addMessage(header, line.getId(), docCode, "PE_WN_NOT_FOUND", defMsgParams);
                continue;
            }

            if  (transformer.getWindNum() == WindingNumber.THREE && (inputMp == null || inputMpH == null || inputMpM == null || inputMpL == null)) {
                messageService.addMessage(header, line.getId(), docCode, "PE_INPUT_NOT_FOUND", defMsgParams);
                continue;
            }

            if  (transformer.getWindNum() == WindingNumber.TWO && (inputMp == null || (inputMpH == null && inputMpL == null))) {
                messageService.addMessage(header, line.getId(), docCode, "PE_INPUT_NOT_FOUND", defMsgParams);
                continue;
            }

            if (sNom  == 0) {
                messageService.addMessage(header, line.getId(), docCode, "PE_SNOM_NOT_FOUND", defMsgParams);
                continue;
            }

            if (uNomH == 0) {
                messageService.addMessage(header, line.getId(), docCode, "PE_UNOMH_NOT_FOUND", defMsgParams);
                continue;
            }

            if (uAvg  == 0) {
                messageService.addMessage(header, line.getId(), docCode, "PE_UAVG_NOT_FOUND", defMsgParams);
                continue;
            }

            if (hours  == 0) continue;


            if (transformer.getWindNum() == WindingNumber.TWO) {
                ParamValue valueH, valueL, value;
                Map<String, String> msgParams = null;
                try {
                    msgParams = buildMsgParams(inputMpL);
                    valueL = getMrVal(inputMpL, context);

                    msgParams = buildMsgParams(inputMpH);
                    valueH = getMrVal(inputMpH, context);

                    value = inputMpH != null ? valueH : valueL;
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                    continue;
                }

                Double resist = transformer.getResist();
                if (resist == null) {
                    messageService.addMessage(header, line.getId(), docCode, "PE_RESIST_NOT_FOUND", defMsgParams);
                    resist = pkzHL * (pow(uNomH, 2) / pow(sNom, 2)) * 1000d;
                }

                Double valXX = deltaPxx * hours * pow(uAvg / uNomH, 2);
                valXX = round(valXX, paramWL);

                Double valN  = value.getTotalE() * resist / (pow(uAvg, 2) * 1000d * hours);
                valN = round(valN, paramWL);

                result.addValueH(valueH)
                    .addValueL(valueL);

                result.setResistH(resist);
                result.setValXX(valXX);
                result.setValN(valN);
                result.setVal(valXX + valN);
            }

            if (transformer.getWindNum() == WindingNumber.THREE) {
                Map<String, String> msgParams = null;
                ParamValue valueH, valueM, valueL;
                try {
                    msgParams = buildMsgParams(inputMpL);
                    valueL = getMrVal(inputMpL, context);

                    msgParams = buildMsgParams(inputMpM);
                    valueM = getMrVal(inputMpM, context);

                    msgParams = buildMsgParams(inputMpH);
                    valueH = getMrVal(inputMpH, context);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                    continue;
                }

                Double resistL = transformer.getResistL();
                Double resistM = transformer.getResistM();
                Double resistH = transformer.getResistH();

                if (resistL == null) {
                    messageService.addMessage(header, line.getId(), docCode, "PE_RESIST_NOT_FOUND", defMsgParams);
                    resistL = (pkzHL + pkzML - pkzHM) / 2d * pow(uNomH / sNom, 2) * 1000d;
                }

                if (resistM == null) {
                    messageService.addMessage(header, line.getId(), docCode, "PE_RESIST_NOT_FOUND", defMsgParams);
                    resistM = (pkzHM + pkzML - pkzHL) / 2d * pow(uNomH / sNom, 2) * 1000d;
                }

                if (resistH == null) {
                    messageService.addMessage(header, line.getId(), docCode, "PE_RESIST_NOT_FOUND", defMsgParams);
                    resistH = (pkzHM + pkzHL - pkzML) / 2d * pow(uNomH / sNom, 2) * 1000d;
                }

                Double valXX = deltaPxx * hours * pow(uAvg / uNomH, 2);
                valXX = round(valXX, paramWL);

                Double valN  = (
                            valueL.getTotalE() * resistL
                        +   valueM.getTotalE() * resistM
                        +   valueH.getTotalE() * resistH
                    )
                    / (pow(uAvg,2) * hours * 1000d);

                valN  = round(valN, paramWL);

                result.addValueH(valueH)
                    .addValueM(valueM)
                    .addValueL(valueL);

                result.setResistH(resistH);
                result.setResistM(resistM);
                result.setResistL(resistL);
                result.setValXX(valXX);
                result.setValN(valN);
                result.setVal(valXX + valN);
            }

            if (result.getMeteringPointOut() != null)
                context.getTransformerValues().putIfAbsent(result.getMeteringPointOut().getCode(), result.getVal());
        }
        return results;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void saveLines(List<PowerTransformerValue> transformerLines) {
        powerTransformerValueRepo.save(transformerLines);
        powerTransformerValueRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void deleteLines(BalanceSubstResultHeader header) {
        List<PowerTransformerValue> transformerValues = powerTransformerValueRepo.findAllByHeaderId(header.getId());
        powerTransformerValueRepo.delete(transformerValues);
        powerTransformerValueRepo.flush();
    }
}
