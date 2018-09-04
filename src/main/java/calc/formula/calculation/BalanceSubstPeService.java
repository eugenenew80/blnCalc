package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.exception.CycleDetectionException;
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
    private static final String docCode = "LOSSES";

    public void calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("Power equipment values for header " + header.getId() + " started");
            header = balanceSubstResultHeaderRepo.findOne(header.getId());
            if (header.getStatus() == BatchStatusEnum.E)
                return;

            updateStatus(header, BatchStatusEnum.P);
            deleteReactorLines(header);
            deleteTransformerLines(header);

            CalcContext context = CalcContext.builder()
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .docCode(docCode)
                .isMeteringReading(true)
                .docId(header.getId())
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
                if (inputMp == null)
                    continue;

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

                if (uNom == 0) continue;

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
                reactorLines.add(reactorLine);
            }

            List<PowerTransformerValue> transformerLines = new ArrayList<>();
            for (BalanceSubstPeLine peLine : header.getHeader().getPeLines()) {
                PowerTransformer transformer = peLine.getPowerTransformer();
                if (transformer == null || transformer.getWindingsNumber() == null)
                    continue;

                MeteringPoint inputMpH = transformer.getInputMpH();
                MeteringPoint inputMpM = transformer.getInputMpM();
                MeteringPoint inputMpL = transformer.getInputMpL();
                MeteringPoint inputMp =  transformer.getInputMp();

                if (inputMp == null || inputMpH == null || inputMpM == null || inputMpL == null)
                    continue;

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

                if (sNom  == 0) continue;
                if (uNomH == 0) continue;
                if (uAvg  == 0) continue;

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

                if (transformer.getWindingsNumber().equals(2l)) {
                    Double totalApEH = getMrVal(inputMpH, "A+", context);
                    Double totalAmEH = getMrVal(inputMpH, "A-", context);
                    Double totalAEH = Optional.ofNullable(totalApEH).orElse(0d) + Optional.ofNullable(totalAmEH).orElse(0d);

                    Double totalRpEH = getMrVal(inputMpH, "R+", context);
                    Double totalRmEH = getMrVal(inputMpH, "R-", context);;
                    Double totalREH = Optional.ofNullable(totalRpEH).orElse(0d) + Optional.ofNullable(totalRmEH).orElse(0d);

                    Double totalEH = Math.pow(totalAEH, 2) + Math.pow(totalREH, 2);
                    Double resistH = pkzHL * (Math.pow(uNomH, 2) / Math.pow(sNom, 2));
                    Double valXX = deltaPxx * operatingTime * Math.pow(uAvg / uNomH, 2);
                    Double valN = totalEH * resistH / (Math.pow(uAvg,2) * operatingTime);

                    transformerLine.setTotalAEH(totalAEH);
                    transformerLine.setTotalREH(totalREH);
                    transformerLine.setTotalEH(totalEH);
                    transformerLine.setResistH(resistH);
                    transformerLine.setValXX(valXX);
                    transformerLine.setValN(valN);
                    transformerLine.setVal(valXX + valN);
                }

                if (transformer.getWindingsNumber().equals(3l)) {
                    Double totalApEL = getMrVal(inputMpL, "A+", context);
                    Double totalAmEL = getMrVal(inputMpL, "A-", context);;
                    Double totalAEL = Optional.ofNullable(totalApEL).orElse(0d) + Optional.ofNullable(totalAmEL).orElse(0d);

                    Double totalRpEL = getMrVal(inputMpL, "R+", context);
                    Double totalRmEL = getMrVal(inputMpL, "R-", context);;
                    Double totalREL = Optional.ofNullable(totalRpEL).orElse(0d) + Optional.ofNullable(totalRmEL).orElse(0d);;

                    Double totalApEM = getMrVal(inputMpM, "A+", context);
                    Double totalAmEM = getMrVal(inputMpM, "A-", context);;
                    Double totalAEM = Optional.ofNullable(totalApEM).orElse(0d) + Optional.ofNullable(totalAmEM).orElse(0d);

                    Double totalRpEM = getMrVal(inputMpM, "R+", context);
                    Double totalRmEM = getMrVal(inputMpM, "R-", context);;
                    Double totalREM = Optional.ofNullable(totalRpEM).orElse(0d) + Optional.ofNullable(totalRmEM).orElse(0d);;

                    Double totalApEH = getMrVal(inputMpH, "A+", context);
                    Double totalAmEH = getMrVal(inputMpH, "A-", context);;
                    Double totalAEH = Optional.ofNullable(totalApEH).orElse(0d) + Optional.ofNullable(totalAmEH).orElse(0d);

                    Double totalRpEH = getMrVal(inputMpH, "R+", context);
                    Double totalRmEH = getMrVal(inputMpH, "R-", context);;
                    Double totalREH = Optional.ofNullable(totalRpEH).orElse(0d) + Optional.ofNullable(totalRmEH).orElse(0d);;

                    Double totalEL = Math.pow(totalAEL, 2) + Math.pow(totalREL, 2);
                    Double totalEM = Math.pow(totalAEM, 2) + Math.pow(totalREM, 2);
                    Double totalEH = Math.pow(totalAEH, 2) + Math.pow(totalREH, 2);

                    Double resistL = (pkzHL + pkzML - pkzHM) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;
                    Double resistM = (pkzHM + pkzML - pkzHL) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;
                    Double resistH = (pkzHM + pkzHL - pkzML) / 2d * Math.pow(uNomH / sNom, 2) * 1000d;

                    Double valXX = deltaPxx * operatingTime * Math.pow(uAvg / uNomH, 2);
                    Double valN = (totalEL * resistL + totalEM * resistM + totalEH * resistH) / (Math.pow(uAvg,2) * operatingTime * 1000d);

                    transformerLine.setTotalAEH(totalAEH);
                    transformerLine.setTotalREH(totalREH);
                    transformerLine.setTotalEH(totalEH);
                    transformerLine.setTotalAEM(totalAEM);
                    transformerLine.setTotalREM(totalREM);
                    transformerLine.setTotalEM(totalEM);
                    transformerLine.setTotalAEL(totalAEL);
                    transformerLine.setTotalREL(totalREL);
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

            reactorValueRepo.save(reactorLines);
            powerTransformerValueRepo.save(transformerLines);
            updateStatus(header, BatchStatusEnum.C);

            logger.info("Power equipment values for header " + header.getId() + " completed");
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Power equipment values for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
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

    private Double getMrVal(MeteringPoint meteringPoint, String param, CalcContext context) throws CycleDetectionException {
        if (meteringPoint == null)
            return null;

        Double value;
        if (meteringPoint.getMeteringPointTypeId().equals(2l)) {
            List<CalcResult> results = calcService.calcMeteringPoints(Arrays.asList(meteringPoint), param, context);
            value = results.size() > 0 ? results.get(0).getDoubleValue() : null;
            logger.info(meteringPoint.getCode() + ", " + param + ", " + value);
        }
        else {
            value = MeteringReadingExpression.builder()
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
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }
}
