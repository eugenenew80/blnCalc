package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.ParamTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
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
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final ReactorValueRepo reactorValueRepo;
    private final PowerTransformerValueRepo powerTransformerValueRepo;
    private final UnitRepo unitRepo;
    private final WorkingHoursService workingHoursService;
    private final ReactorService reactorService;
    private final PowerTransformerService powerTransformerService;
    private final BsResultUavgService uavgService;
    private final BsResultMrService mrService;
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
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .headerId(header.getId())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .docCode(docCode)
                .docId(header.getId())
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            Unit unit = unitRepo.findByCode("kW.h");
            List<BalanceSubstResultMrLine> mrLines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());

            List<ReactorValue> reactorLines = new ArrayList<>();
            for (BalanceSubstPeLine peLine : header.getHeader().getPeLines()) {
                Reactor reactor = peLine.getReactor();
                if (reactor == null)
                    continue;

                MeteringPoint inputMp = reactor.getInputMp();
                if (inputMp == null)
                    continue;

                Double uNom = ReactorExpression.builder()
                    .id(reactor.getId())
                    .attr("unom")
                    .def(0d)
                    .context(context)
                    .service(reactorService)
                    .build()
                    .doubleValue();

                Double deltaPr = ReactorExpression.builder()
                    .id(reactor.getId())
                    .attr("delta_pr")
                    .def(0d)
                    .context(context)
                    .service(reactorService)
                    .build()
                    .doubleValue();

                Double hours = WorkingHoursExpression.builder()
                    .objectType("re")
                    .objectId(reactor.getId())
                    .context(context)
                    .service(workingHoursService)
                    .build()
                    .doubleValue();

                Double uAvg = UavgExpression.builder()
                    .meteringPointCode(inputMp.getCode())
                    .def(inputMp.getVoltageClass().getValue())
                    .context(context)
                    .service(uavgService)
                    .build()
                    .doubleValue();

                uAvg = uAvg / 1000d;

                if (uNom == 0) continue;
                if (deltaPr == 0) continue;

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
                if (transformer == null)
                    continue;

                MeteringPoint inputMp = transformer.getInputMp();
                if (inputMp == null)
                    continue;

                Double sNom     = getTransformerAttr(transformer, "snom",       context);
                Double uNomH    = getTransformerAttr(transformer, "unom_h",     context);
                Double deltaPxx = getTransformerAttr(transformer, "delta_pxx",  context);
                Double pkzHM    = getTransformerAttr(transformer, "pkz_hm",     context);
                Double pkzML    = getTransformerAttr(transformer, "pkz_ml",     context);
                Double pkzHL    = getTransformerAttr(transformer, "pkz_hl",     context);

                Double hours = WorkingHoursExpression.builder()
                    .objectType("tr")
                    .objectId(transformer.getId())
                    .context(context)
                    .service(workingHoursService)
                    .build()
                    .doubleValue();

                Double uAvg = UavgExpression.builder()
                    .meteringPointCode(inputMp!=null ? inputMp.getCode() : "")
                    .def(inputMp!=null && inputMp.getVoltageClass()!=null ? inputMp.getVoltageClass().getValue() : 0d)
                    .context(context)
                    .service(uavgService)
                    .build()
                    .doubleValue();

                uAvg = uAvg / 1000d;

                if (sNom == 0)  continue;
                if (uNomH == 0) continue;
                if (uAvg == 0)  continue;

                MeteringPoint inputMpH = transformer.getInputMpH();
                MeteringPoint inputMpM = transformer.getInputMpM();
                MeteringPoint inputMpL = transformer.getInputMpL();

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
                transformerLine.setOperatingTime(hours);
                transformerLine.setUavg(uAvg);
                transformerLine.setWindingsNumber(transformer.getWindingsNumber());

                PeriodTypeEnum periodType = header.getPeriodType();
                if (transformer.getWindingsNumber() == null || transformer.getWindingsNumber() == 2) {
                    Double totalApEH = getPtValue(inputMpH, "A+", periodType, context);
                    Double totalAmEH = getPtValue(inputMpH, "A-", periodType, context);;
                    Double totalAEH = Optional.ofNullable(totalApEH).orElse(0d) + Optional.ofNullable(totalAmEH).orElse(0d);

                    Double totalRpEH = getPtValue(inputMpH, "R+", periodType, context);
                    Double totalRmEH = getPtValue(inputMpH, "R-", periodType, context);;
                    Double totalREH = Optional.ofNullable(totalRpEH).orElse(0d) + Optional.ofNullable(totalRmEH).orElse(0d);;

                    Double totalEH = Math.pow(totalAEH, 2) + Math.pow(totalREH, 2);
                    Double resistH = pkzHL * (Math.pow(uNomH, 2) / Math.pow(sNom, 2));
                    Double valXX = deltaPxx * hours * Math.pow(uAvg / uNomH, 2);
                    Double valN = totalEH * resistH / (Math.pow(uAvg,2) * hours);

                    transformerLine.setTotalAEH(totalAEH);
                    transformerLine.setTotalREH(totalREH);
                    transformerLine.setTotalEH(totalEH);
                    transformerLine.setResistH(resistH);
                    transformerLine.setValXX(valXX);
                    transformerLine.setValN(valN);
                    transformerLine.setVal(valXX + valN);
                }

                if (transformer.getWindingsNumber()!=null && transformer.getWindingsNumber()==3) {
                    Double totalApEL = getPtValue(inputMpL, "A+", periodType, context);
                    Double totalAmEL = getPtValue(inputMpL, "A-", periodType, context);;
                    Double totalAEL = Optional.ofNullable(totalApEL).orElse(0d) + Optional.ofNullable(totalAmEL).orElse(0d);

                    Double totalRpEL = getPtValue(inputMpL, "R+", periodType, context);
                    Double totalRmEL = getPtValue(inputMpL, "R-", periodType, context);;
                    Double totalREL = Optional.ofNullable(totalRpEL).orElse(0d) + Optional.ofNullable(totalRmEL).orElse(0d);;

                    Double totalApEM = getPtValue(inputMpM, "A+", periodType, context);
                    Double totalAmEM = getPtValue(inputMpM, "A-", periodType, context);;
                    Double totalAEM = Optional.ofNullable(totalApEM).orElse(0d) + Optional.ofNullable(totalAmEM).orElse(0d);

                    Double totalRpEM = getPtValue(inputMpM, "R+", periodType, context);
                    Double totalRmEM = getPtValue(inputMpM, "R-", periodType, context);;
                    Double totalREM = Optional.ofNullable(totalRpEM).orElse(0d) + Optional.ofNullable(totalRmEM).orElse(0d);;

                    Double totalApEH = getPtValue(inputMpH, "A+", periodType, context);
                    Double totalAmEH = getPtValue(inputMpH, "A-", periodType, context);;
                    Double totalAEH = Optional.ofNullable(totalApEH).orElse(0d) + Optional.ofNullable(totalAmEH).orElse(0d);

                    Double totalRpEH = getPtValue(inputMpH, "R+", periodType, context);
                    Double totalRmEH = getPtValue(inputMpH, "R-", periodType, context);;
                    Double totalREH = Optional.ofNullable(totalRpEH).orElse(0d) + Optional.ofNullable(totalRmEH).orElse(0d);;


                    Double totalEL = Math.pow(totalAEL, 2) + Math.pow(totalREL, 2);
                    Double totalEM = Math.pow(totalAEM, 2) + Math.pow(totalREM, 2);
                    Double totalEH = inputMpH != null ? Math.pow(totalAEH, 2) + Math.pow(totalREH, 2) : totalEL + totalAEM;

                    Double resistL = (pkzHL + pkzML - pkzHM) / (2d * 1000d) * (Math.pow(uNomH,2) / Math.pow(sNom,2));
                    Double resistM = (pkzHM + pkzML - pkzHL) / (2d * 1000d) * (Math.pow(uNomH,2) / Math.pow(sNom,2));
                    Double resistH = (pkzHM + pkzHL - pkzML) / (2d * 1000d) * (Math.pow(uNomH,2) / Math.pow(sNom,2));

                    Double valXX = deltaPxx * hours * Math.pow(uAvg / uNomH, 2);
                    Double valN = (totalEL * resistL + totalEM * resistM + totalEH * resistH) / (Math.pow(uAvg,2) * hours);

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

    private Double getPtValue(MeteringPoint meteringPoint, String param, PeriodTypeEnum periodType, CalcContext context) throws CycleDetectionException {
        if (meteringPoint == null)
            return null;

        Double totalApH = null;
        if (meteringPoint.getMeteringPointTypeId().equals(2l)) {
            Formula formula = meteringPoint.getFormulas().stream()
                .filter(t -> t.getParam().getCode().equals(param))
                .filter(t -> t.getParamType() == ParamTypeEnum.PT)
                .filter(t -> t.getPeriodType() == periodType)
                .findFirst()
                .orElse(null);

            if (formula != null) {
                List<CalcResult> results = calcService.calcFormulas(Arrays.asList(formula), context);
                totalApH = results.size() > 0 ? results.get(0).getDoubleValue() : null;
            }
        }
        else {
            totalApH = MeteringReadingExpression.builder()
                .meteringPointCode(meteringPoint.getCode())
                .parameterCode(param)
                .context(context)
                .service(mrService)
                .build()
                .doubleValue();
        }

        return totalApH;
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
