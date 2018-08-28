package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.expression.impl.PowerTransformerExpression;
import calc.formula.expression.impl.ReactorExpression;
import calc.formula.expression.impl.UavgExpression;
import calc.formula.expression.impl.WorkingHoursExpression;
import calc.formula.service.PowerTransformerService;
import calc.formula.service.ReactorService;
import calc.formula.service.UavgService;
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
    private final ReactorValueRepo reactorValueRepo;
    private final PowerTransformerValueRepo powerTransformerValueRepo;
    private final UnitRepo unitRepo;
    private final WorkingHoursService workingHoursService;
    private final ReactorService reactorService;
    private final PowerTransformerService powerTransformerService;
    private final UavgService uavgService;
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
                    .attr(uNom())
                    .def(0d)
                    .context(context)
                    .service(reactorService)
                    .build()
                    .doubleValue();

                Double deltaPr = ReactorExpression.builder()
                    .id(reactor.getId())
                    .attr(deltaPr())
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
                    .headerId(header.getId())
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

                Double sNom = PowerTransformerExpression.builder()
                    .id(transformer.getId())
                    .attr(sNom())
                    .def(0d)
                    .context(context)
                    .service(powerTransformerService)
                    .build()
                    .doubleValue();

                Double uNomH = PowerTransformerExpression.builder()
                    .id(transformer.getId())
                    .attr(uNomH())
                    .def(0d)
                    .context(context)
                    .service(powerTransformerService)
                    .build()
                    .doubleValue();

                Double deltaPxx = PowerTransformerExpression.builder()
                    .id(transformer.getId())
                    .attr(deltaPxx())
                    .def(0d)
                    .context(context)
                    .service(powerTransformerService)
                    .build()
                    .doubleValue();

                Double pkzHM = PowerTransformerExpression.builder()
                    .id(transformer.getId())
                    .attr(pkzHM())
                    .def(0d)
                    .context(context)
                    .service(powerTransformerService)
                    .build()
                    .doubleValue();

                Double pkzML = PowerTransformerExpression.builder()
                    .id(transformer.getId())
                    .attr(pkzML())
                    .def(0d)
                    .context(context)
                    .service(powerTransformerService)
                    .build()
                    .doubleValue();

                Double pkzHL = PowerTransformerExpression.builder()
                    .id(transformer.getId())
                    .attr(pkzHL())
                    .def(0d)
                    .context(context)
                    .service(powerTransformerService)
                    .build()
                    .doubleValue();

                Double hours = WorkingHoursExpression.builder()
                    .objectType("tr")
                    .objectId(transformer.getId())
                    .context(context)
                    .service(workingHoursService)
                    .build()
                    .doubleValue();

                Double uAvg = UavgExpression.builder()
                    .headerId(header.getId())
                    .meteringPointCode(inputMp!=null ? inputMp.getCode() : "")
                    .def(inputMp!=null && inputMp.getVoltageClass()!=null ? inputMp.getVoltageClass().getValue() : 0d)
                    .context(context)
                    .service(uavgService)
                    .build()
                    .doubleValue();

                uAvg = uAvg / 1000d;


                if (sNom == 0) continue;
                if (uNomH == 0) continue;
                if (uAvg == 0) continue;

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

                if (transformer.getWindingsNumber() == null || transformer.getWindingsNumber() == 2) {
                    Double totalAeH = 0d;
                    Double totalReH = 0d;
                    if (inputMpH!=null) {
                        totalAeH = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(inputMpH))
                            .filter(t -> t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-"))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);

                        totalReH = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(inputMpH))
                            .filter(t -> t.getParam().getCode().equals("R+") || t.getParam().getCode().equals("R-"))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);
                    }

                    Double totalEh = Math.pow(totalAeH, 2) + Math.pow(totalReH, 2);
                    Double resistH = pkzHL * (Math.pow(uNomH, 2) / Math.pow(sNom, 2));
                    Double valXX = deltaPxx * hours * Math.pow(uAvg / uNomH, 2);
                    Double valN = totalEh * resistH / (Math.pow(uAvg,2) * hours);

                    transformerLine.setTotalAEH(totalAeH);
                    transformerLine.setTotalREH(totalReH);
                    transformerLine.setTotalEH(totalEh);
                    transformerLine.setResistH(resistH);
                    transformerLine.setValXX(valXX);
                    transformerLine.setValN(valN);
                    transformerLine.setVal(valXX + valN);
                }

                if (transformer.getWindingsNumber()!=null && transformer.getWindingsNumber()==3) {
                    Double totalAeL = 0d;
                    Double totalReL = 0d;
                    if (inputMpL!=null) {
                        totalAeL = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(inputMpL))
                            .filter(t -> t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-"))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);

                        totalReL = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(inputMpL))
                            .filter(t -> t.getParam().getCode().equals("R+") || t.getParam().getCode().equals("R-"))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);
                    }

                    Double totalAeM = 0d;
                    Double totalReM = 0d;
                    if (inputMpM!=null) {
                        totalAeM = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(inputMpM))
                            .filter(t -> t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-"))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);

                        totalReM = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(inputMpM))
                            .filter(t -> t.getParam().getCode().equals("R+") || t.getParam().getCode().equals("R-"))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);
                    }

                    Double totalAeH = 0d;
                    Double totalReH = 0d;
                    if (inputMpH!=null) {
                        totalAeH = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(inputMpH))
                            .filter(t -> t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-"))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);

                        totalReH = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(inputMpH))
                            .filter(t -> t.getParam().getCode().equals("R+") || t.getParam().getCode().equals("R-"))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);
                    }

                    Double totalEL = Math.pow(totalAeL, 2) + Math.pow(totalReL, 2);
                    Double totalEM = Math.pow(totalAeM, 2) + Math.pow(totalReM, 2);
                    Double totalEH = inputMpH != null ? Math.pow(totalAeH, 2) + Math.pow(totalReH, 2) : totalEL + totalAeM;

                    Double resistL = (pkzHL + pkzML - pkzHM) / (2d * 1000d) * (Math.pow(uNomH,2) / Math.pow(sNom,2));
                    Double resistM = (pkzHM + pkzML - pkzHL) / (2d * 1000d) * (Math.pow(uNomH,2) / Math.pow(sNom,2));
                    Double resistH = (pkzHM + pkzHL - pkzML) / (2d * 1000d) * (Math.pow(uNomH,2) / Math.pow(sNom,2));

                    Double valXX = deltaPxx * hours * Math.pow(uAvg / uNomH, 2);
                    Double valN = (totalEL * resistL + totalEM * resistM + totalEH * resistH) / (Math.pow(uAvg,2) * hours);

                    transformerLine.setTotalAEH(totalAeH);
                    transformerLine.setTotalREH(totalReH);
                    transformerLine.setTotalEH(totalEH);
                    transformerLine.setTotalAEM(totalAeM);
                    transformerLine.setTotalREM(totalReM);
                    transformerLine.setTotalEM(totalEM);
                    transformerLine.setTotalAEL(totalAeL);
                    transformerLine.setTotalREL(totalReL);
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

    private static String sNom() { return "snom"; }
    private static String uNomH() { return "unom_h"; }
    private static String uNom() { return "unom"; }
    private static String deltaPr() { return "delta_pr"; }
    private static String deltaPxx() { return "delta_pxx"; }
    private static String pkzHM() { return "pkz_hm"; }
    private static String pkzML() { return "pkz_ml"; }
    private static String pkzHL() { return "pkz_hl"; }
}
