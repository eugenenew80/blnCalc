package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.expression.impl.*;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceSubstPeService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstPeService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final ReactorValueRepo reactorValueRepo;
    private final PowerTransformerValueRepo powerTransformerValueRepo;
    private final UnitRepo unitRepo;
    private final WorkingHoursService workingHoursService;
    private final ReactorService reactorService;
    private final PowerTransformerService powerTransformerService;
    private final BsResultUavgService resultUavgService;
    private final BsResultMrService resultMrService;
    private final CalcService calcService;
    private final MessageService messageService;
    private static final String docCode = "LOSSES";

    public boolean calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("Power equipment values for header " + header.getId() + " started");
            header = balanceSubstResultHeaderRepo.findOne(header.getId());
            if (header.getStatus() == BatchStatusEnum.E)
                return false;

            updateStatus(header, BatchStatusEnum.P);
            deleteReactorLines(header);
            deleteTransformerLines(header);
            deleteMessages(header, docCode);

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .docId(header.getId())
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .isMeteringReading(true)
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            Unit unit = unitRepo.findByCode("kW.h");

            List<ReactorValue> reactorLines = new ArrayList<>();
            for (BalanceSubstPeLine peLine : header.getHeader().getPeLines()) {
                Reactor reactor = peLine.getReactor();
                if (reactor == null)
                    continue;

                MeteringPoint inputMp = reactor.getInputMp();
                if (inputMp == null) {
                    messageService.addMessage(header, peLine.getId(), docCode, "PE_INPUT_NOT_FOUND");
                    continue;
                }

                Double uNom    = getReactorAttr(reactor, "unom",     context);
                Double deltaPr = getReactorAttr(reactor, "delta_pr", context);

                Double hours = WorkingHoursExpression.builder()
                    .objectType("re")
                    .objectId(reactor.getId())
                    .context(context)
                    .service(workingHoursService)
                    .build()
                    .doubleValue();

                Double uAvg = UavgExpression.builder()
                    .meteringPointCode(inputMp.getCode())
                    .def(inputMp.getVoltageClass().getValue() / 1000d)
                    .context(context)
                    .service(resultUavgService)
                    .build()
                    .doubleValue();

                if (uNom == 0) {
                    messageService.addMessage(header, peLine.getId(), docCode, "UNOM_NOT_FOUND");
                    continue;
                }

                Double val = deltaPr * hours * Math.pow(uAvg / uNom, 2);

                ReactorValue reactorLine = new ReactorValue();
                reactorLine.setHeader(header);
                reactorLine.setReactor(reactor);
                reactorLine.setDeltaPr(deltaPr);
                reactorLine.setOperatingTime(hours);
                reactorLine.setUavg(uAvg);
                reactorLine.setUnom(uNom);
                reactorLine.setUnit(unit);
                reactorLine.setVal(val);
                reactorLine.setInputMp(inputMp);
                reactorLine.setMeteringPointOut(peLine.getMeteringPointOut());
                reactorLines.add(reactorLine);
            }

            List<PowerTransformerValue> transformerLines = new ArrayList<>();
            for (BalanceSubstPeLine peLine : header.getHeader().getPeLines()) {
                PowerTransformer transformer = peLine.getPowerTransformer();
                if (transformer == null)
                    continue;

                if (transformer.getWindingsNumber() == null) {
                    messageService.addMessage(header, peLine.getId(), docCode, "WN_NOT_FOUND");
                    continue;
                }

                MeteringPoint inputMpH = transformer.getInputMpH();
                MeteringPoint inputMpM = transformer.getInputMpM();
                MeteringPoint inputMpL = transformer.getInputMpL();
                MeteringPoint inputMp =  transformer.getInputMp();

                if  (transformer.getWindingsNumber().equals(3l) && (inputMp == null || inputMpH == null || inputMpM == null || inputMpL == null)) {
                    messageService.addMessage(header, peLine.getId(), docCode, "PE_INPUT_NOT_FOUND");
                    continue;
                }

                if  (transformer.getWindingsNumber().equals(2l) && (inputMp == null || inputMpH == null)) {
                    messageService.addMessage(header, peLine.getId(), docCode, "PE_INPUT_NOT_FOUND");
                    continue;
                }

                Double sNom     = getTransformerAttr(transformer, "snom",      context);
                Double uNomH    = getTransformerAttr(transformer, "unom_h",    context);
                Double deltaPxx = getTransformerAttr(transformer, "delta_pxx", context);
                Double pkzHM    = getTransformerAttr(transformer, "pkz_hm",    context);
                Double pkzML    = getTransformerAttr(transformer, "pkz_ml",    context);
                Double pkzHL    = getTransformerAttr(transformer, "pkz_hl",    context);

                if (transformer.getWindingsNumber().equals(3l)) {
                    pkzHL = pkzHL / Math.pow(150d / 501d, 2);
                    pkzML = pkzML / Math.pow(150d / 501d, 2);
                }

                Double operatingTime = WorkingHoursExpression.builder()
                    .objectType("tr")
                    .objectId(transformer.getId())
                    .context(context)
                    .service(workingHoursService)
                    .build()
                    .doubleValue();

                Double uAvg = UavgExpression.builder()
                    .meteringPointCode(inputMp.getCode())
                    .def(inputMp.getVoltageClass()!=null ? inputMp.getVoltageClass().getValue() / 1000d : 0d)
                    .context(context)
                    .service(resultUavgService)
                    .build()
                    .doubleValue();

                if (sNom  == 0) {
                    messageService.addMessage(header, peLine.getId(), docCode, "SNOM_NOT_FOUND");
                    continue;
                }

                if (uNomH == 0) {
                    messageService.addMessage(header, peLine.getId(), docCode, "UNOMH_NOT_FOUND");
                    continue;
                }

                if (uAvg  == 0) {
                    messageService.addMessage(header, peLine.getId(), docCode, "UAVG_NOT_FOUND");
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
                transformerLine.setUnit(unit);
                transformerLine.setOperatingTime(operatingTime);
                transformerLine.setUavg(uAvg);
                transformerLine.setWindingsNumber(transformer.getWindingsNumber());
                transformerLine.setMeteringPointOut(peLine.getMeteringPointOut());

                if (transformer.getWindingsNumber().equals(2l)) {
                    Double apH = getMrVal(inputMpH, "A+", context);
                    Double amH = getMrVal(inputMpH, "A-", context);
                    Double totalAH = Optional.ofNullable(apH).orElse(0d) + Optional.ofNullable(amH).orElse(0d);

                    Double rpH = getMrVal(inputMpH, "R+", context);
                    Double rmH = getMrVal(inputMpH, "R-", context);;
                    Double totalRH = Optional.ofNullable(rpH).orElse(0d) + Optional.ofNullable(rmH).orElse(0d);

                    Double totalEH = Math.pow(totalAH, 2) + Math.pow(totalRH, 2);
                    Double resistH = pkzHL * (Math.pow(uNomH, 2) / Math.pow(sNom, 2));
                    Double valXX = deltaPxx * operatingTime * Math.pow(uAvg / uNomH, 2);
                    Double valN = totalEH * resistH / (Math.pow(uAvg,2) * operatingTime);

                    transformerLine.setApH(apH);
                    transformerLine.setAmH(amH);
                    transformerLine.setRpH(rpH);
                    transformerLine.setRmH(rmH);
                    transformerLine.setTotalAEH(totalAH);
                    transformerLine.setTotalREH(totalRH);
                    transformerLine.setTotalEH(totalEH);
                    transformerLine.setResistH(resistH);
                    transformerLine.setValXX(valXX);
                    transformerLine.setValN(valN);
                    transformerLine.setVal(valXX + valN);
                }

                if (transformer.getWindingsNumber().equals(3l)) {
                    Double apL = getMrVal(inputMpL, "A+", context);
                    Double amL = getMrVal(inputMpL, "A-", context);;
                    Double totalAL = Optional.ofNullable(apL).orElse(0d) + Optional.ofNullable(amL).orElse(0d);

                    Double rpL = getMrVal(inputMpL, "R+", context);
                    Double rmL = getMrVal(inputMpL, "R-", context);;
                    Double totalRL = Optional.ofNullable(rpL).orElse(0d) + Optional.ofNullable(rmL).orElse(0d);;

                    Double apM = getMrVal(inputMpM, "A+", context);
                    Double amM = getMrVal(inputMpM, "A-", context);;
                    Double totalAM = Optional.ofNullable(apM).orElse(0d) + Optional.ofNullable(amM).orElse(0d);

                    Double rpM = getMrVal(inputMpM, "R+", context);
                    Double rmM = getMrVal(inputMpM, "R-", context);;
                    Double totalRM = Optional.ofNullable(rpM).orElse(0d) + Optional.ofNullable(rmM).orElse(0d);;

                    Double apH = getMrVal(inputMpH, "A+", context);
                    Double amH = getMrVal(inputMpH, "A-", context);;
                    Double totalAH = Optional.ofNullable(apH).orElse(0d) + Optional.ofNullable(amH).orElse(0d);

                    Double rpH = getMrVal(inputMpH, "R+", context);
                    Double rmH = getMrVal(inputMpH, "R-", context);;
                    Double totalRH = Optional.ofNullable(rpH).orElse(0d) + Optional.ofNullable(rmH).orElse(0d);;

                    Double totalEL = Math.pow(totalAL, 2) + Math.pow(totalRL, 2);
                    Double totalEM = Math.pow(totalAM, 2) + Math.pow(totalRM, 2);
                    Double totalEH = Math.pow(totalAH, 2) + Math.pow(totalRH, 2);

                    Double resistL = (pkzHL + pkzML - pkzHM) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;
                    Double resistM = (pkzHM + pkzML - pkzHL) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;
                    Double resistH = (pkzHM + pkzHL - pkzML) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;

                    Double valXX = deltaPxx * operatingTime * Math.pow(uAvg / uNomH, 2);
                    Double valN = (totalEL * resistL + totalEM * resistM + totalEH * resistH) / (Math.pow(uAvg,2) * operatingTime * 1000d);

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
                    transformerLine.setVal(valXX + valN);
                }

                transformerLines.add(transformerLine);
            }

            saveReactorLines(reactorLines);
            saveTransformerLines(transformerLines);
            updateStatus(header, BatchStatusEnum.C);

            logger.info("Power equipment values for header " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION");
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Power equipment values for header " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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

    private Double getReactorAttr(Reactor reactor, String attr, CalcContext context) {
        return ReactorExpression.builder()
            .id(reactor.getId())
            .attr(attr)
            .def(0d)
            .context(context)
            .service(reactorService)
            .build()
            .doubleValue();
    }

    private Double getMrVal(MeteringPoint meteringPoint, String param, CalcContext context) throws Exception {
        if (meteringPoint == null)
            return null;

        Double value;
        if (meteringPoint.getMeteringPointTypeId().equals(2l)) {
            List<CalcResult> results = calcService.calcMeteringPoints(Arrays.asList(meteringPoint), param, context);
            value = results.size() > 0 ? results.get(0).getDoubleValue() : null;
        }
        else {
            value = MrExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param)
                .rate(1d)
                .context(context)
                .service(resultMrService)
                .build()
                .doubleValue();
        }

        return value;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveTransformerLines(List<PowerTransformerValue> transformerLines) {
        powerTransformerValueRepo.save(transformerLines);
        powerTransformerValueRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveReactorLines(List<ReactorValue> reactorLines) {
        reactorValueRepo.save(reactorLines);
        reactorValueRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteReactorLines(BalanceSubstResultHeader header) {
        List<ReactorValue> lines = reactorValueRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            reactorValueRepo.delete(lines.get(i));
        reactorValueRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteTransformerLines(BalanceSubstResultHeader header) {
        List<PowerTransformerValue> lines = powerTransformerValueRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            powerTransformerValueRepo.delete(lines.get(i));
        powerTransformerValueRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(BalanceSubstResultHeader header, String docCode) {
        messageService.deleteMessages(header, docCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }
}
