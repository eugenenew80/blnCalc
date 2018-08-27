package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.expression.impl.WorkingHoursExpression;
import calc.formula.service.WorkingHoursService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BalanceSubstPeService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstPeService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;
    private final ReactorValueRepo reactorValueRepo;
    private final PowerTransformerValueRepo powerTransformerValueRepo;
    private final UnitRepo unitRepo;
    private static final String docCode = "LOSSES";
    private final WorkingHoursService workingHoursService;

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
            List<BalanceSubstResultULine> uLines = balanceSubstResultULineRepo.findAllByHeaderId(header.getId());

            List<ReactorValue> reactorLines = new ArrayList<>();
            for (BalanceSubstPeLine peLine : header.getHeader().getPeLines()) {
                if (peLine.getReactor() == null)
                    continue;

                Reactor reactor = peLine.getReactor();
                if (reactor.getInputMp() == null) {
                    logger.warn("Input metering point not found for reactor id: " + reactor.getId());
                    continue;
                }

                Double hours = WorkingHoursExpression.builder()
                    .objectType("re")
                    .objectId(reactor.getId())
                    .context(context)
                    .service(workingHoursService)
                    .build()
                    .doubleValue();

                MeteringPoint inputMp = reactor.getInputMp();
                Double uavg = uLines.stream()
                    .filter(t -> t.getMeteringPoint().equals(inputMp))
                    .filter(t -> t.getVal() != null && t.getVal() != 0)
                    .map(t -> t.getVal())
                    .findFirst()
                    .orElse(inputMp.getRatedVoltage());

                if (reactor.getInputMp() == null) {
                    logger.warn("Uavg not found for reactor id: " + reactor.getId());
                    continue;
                }

                Double deltaPr = Optional.of(reactor.getDeltaPr()).orElse(0d);
                Double unom = Optional.of(reactor.getUnom()).orElse(0d);

                if (reactor.getInputMp() == null) {
                    logger.warn("Unom not found for reactor id: " + reactor.getId());
                    continue;
                }

                Double val = deltaPr * hours * Math.pow(uavg / unom, 2);

                ReactorValue reactorLine = new ReactorValue();
                reactorLine.setHeader(header);
                reactorLine.setReactor(reactor);
                reactorLine.setDeltaPr(deltaPr);
                reactorLine.setOperatingTime(hours);
                reactorLine.setUavg(uavg);
                reactorLine.setUnom(unom);
                reactorLine.setUnit(unit);
                reactorLine.setVal(val);
                reactorLine.setInputMp(inputMp);
                reactorLines.add(reactorLine);
            }

            List<PowerTransformerValue> transformerLines = new ArrayList<>();
            for (BalanceSubstPeLine peLine : header.getHeader().getPeLines()) {
                if (peLine.getPowerTransformer() == null)
                    continue;

                PowerTransformer transformer = peLine.getPowerTransformer();
                MeteringPoint inputMp = transformer.getInputMp();
                MeteringPoint inputMpH = transformer.getInputMpH();
                MeteringPoint inputMpM = transformer.getInputMpM();
                MeteringPoint inputMpL = transformer.getInputMpL();

                Double pkzHM = Optional.ofNullable(transformer.getPkzHM()).orElse(0d);
                Double pkzML = Optional.ofNullable(transformer.getPkzML()).orElse(0d);
                Double pkzHL = Optional.ofNullable(transformer.getPkzHL()).orElse(0d);

                Double deltaPzz = Optional.ofNullable(transformer.getDeltaPxx()).orElse(0d);
                Double snom = Optional.ofNullable(transformer.getSnom()).orElse(0d);
                Double unomH = Optional.ofNullable(transformer.getUnomH()).orElse(0d);

                Double hours = WorkingHoursExpression.builder()
                    .objectType("tr")
                    .objectId(transformer.getId())
                    .context(context)
                    .service(workingHoursService)
                    .build()
                    .doubleValue();

                Double uavg = uLines.stream()
                    .filter(t-> inputMp != null)
                    .filter(t -> t.getMeteringPoint().equals(inputMp))
                    .filter(t -> t.getVal() != null && t.getVal() != 0)
                    .map(t -> t.getVal())
                    .findFirst()
                    .orElse(inputMp.getRatedVoltage());

                PowerTransformerValue transformerLine = new PowerTransformerValue();
                transformerLine.setHeader(header);
                transformerLine.setTransformer(transformer);
                transformerLine.setDeltaPXX(deltaPzz);
                transformerLine.setSnom(snom);
                transformerLine.setUnomH(unomH);
                transformerLine.setInputMp(inputMp);
                transformerLine.setInputMpH(inputMpH);
                transformerLine.setInputMpM(inputMpM);
                transformerLine.setInputMpL(inputMpL);
                transformerLine.setPkzHM(pkzHM);
                transformerLine.setPkzML(pkzML);
                transformerLine.setPkzHL(pkzHL);
                transformerLine.setUnit(unit);
                transformerLine.setOperatingTime(hours);
                transformerLine.setUavg(uavg);

                Double valN;
                Double valXX;
                if (transformer.getWindingsNumber()==null || transformer.getWindingsNumber()==2) {
                    Double totalAeH = mrLines.stream()
                        .filter(t-> inputMp != null)
                        .filter(t -> !t.getIsIgnore())
                        .filter(t -> t.getMeteringPoint().equals(inputMp))
                        .filter(t -> t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-"))
                        .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                        .reduce((t1, t2) -> t1 + t2)
                        .orElse(0d);

                    Double totalReH = mrLines.stream()
                         .filter(t-> inputMp != null)
                        .filter(t -> !t.getIsIgnore())
                        .filter(t -> t.getMeteringPoint().equals(inputMp))
                        .filter(t -> t.getParam().getCode().equals("R+") || t.getParam().getCode().equals("R-"))
                        .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                        .reduce((t1, t2) -> t1 + t2)
                        .orElse(0d);

                    Double totalEh = Math.pow(totalAeH, 2) + Math.pow(totalReH, 2);
                    Double resistH = (pkzHL / 1000d) * (Math.pow(unomH, 2) / Math.pow(snom, 2));
                    valXX = deltaPzz * hours * Math.pow(uavg / unomH, 2);
                    valN = totalEh / (Math.pow(uavg,2) * hours) * resistH;

                    transformerLine.setTotalAEH(totalAeH);
                    transformerLine.setTotalREH(totalReH);
                    transformerLine.setTotalEH(totalEh);
                    transformerLine.setValXX(valXX);
                    transformerLine.setValN(valN);
                    transformerLine.setVal(valXX + valN);
                }

                if (transformer.getWindingsNumber()!=null && transformer.getWindingsNumber()==3) {

                }

                transformerLines.add(transformerLine);
            }

            //calcValues(header);

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

    private void calcValues(BalanceSubstResultHeader header) {
        balanceSubstResultHeaderRepo.calcPeValues(header.getId());
    }
}
