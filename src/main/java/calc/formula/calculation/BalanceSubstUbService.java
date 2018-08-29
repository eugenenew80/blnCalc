package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.expression.impl.UavgExpression;
import calc.formula.expression.impl.WorkingHoursExpression;
import calc.formula.service.BsResultUavgService;
import calc.formula.service.WorkingHoursService;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import calc.repo.calc.BalanceSubstResultMrLineRepo;
import calc.repo.calc.BalanceSubstResultULineRepo;
import calc.repo.calc.BalanceSubstResultUbLineRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class BalanceSubstUbService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstUbService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final BalanceSubstResultUbLineRepo balanceSubstResultUbLineRepo;
    private final BalanceSubstResultULineRepo balanceSubstResultULineRepo;
    private final WorkingHoursService workingHoursService;
    private final BsResultUavgService resultUavgService;
    private static final String docCode = "UNBALANCE";

    public void calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Unbalance for header " + header.getId() + " started");
            header = balanceSubstResultHeaderRepo.findOne(header.getId());
            if (header.getStatus() == BatchStatusEnum.E)
                return;

            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);

            CalcContext context = CalcContext.builder()
                .headerId(header.getId())
                .periodType(header.getPeriodType())
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

            List<BalanceSubstResultMrLine> mrLines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());

            Double wSum1 = header.getHeader().getUbLines()
                .stream()
                .filter(t -> t.getIsSection1())
                .flatMap(t -> mrLines.stream().filter(l -> !l.getIsIgnore() && l.getMeteringPoint().equals(t.getMeteringPoint())))
                .filter(t -> t.getParam().getCode().equals("A+"))
                .filter(t -> t.getVal() != null)
                .map(t -> t.getVal())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double wSum2 = header.getHeader().getUbLines()
                .stream()
                .filter(t -> t.getIsSection2())
                .flatMap(t -> mrLines.stream().filter(l -> !l.getIsIgnore() && l.getMeteringPoint().equals(t.getMeteringPoint())))
                .filter(t -> t.getParam().getCode().equals("A-"))
                .filter(t -> t.getVal() != null)
                .map(t -> t.getVal())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            List<BalanceSubstResultUbLine> lines = new ArrayList<>();
            for (BalanceSubstUbLine ubLine : header.getHeader().getUbLines()) {
                MeteringPoint meteringPoint = ubLine.getMeteringPoint();

                for (String direction : Arrays.asList("1", "2")) {
                    if (direction.equals("1") && !ubLine.getIsSection1()) continue;
                    if (direction.equals("2") && !ubLine.getIsSection2()) continue;

                    List<MeterHistory> meterHistories = mrLines.stream()
                        .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                        .filter(t -> !t.getIsIgnore())
                        .filter(t -> t.getMeter() != null && t.getMeterHistory() != null)
                        .map(t -> t.getMeterHistory())
                        .distinct()
                        .collect(toList());

                    Double workHours = WorkingHoursExpression.builder()
                        .objectType("mp")
                        .objectId(meteringPoint.getId())
                        .service(workingHoursService)
                        .context(context)
                        .build()
                        .doubleValue();

                    MeteringPoint inputMp = balanceSubstResultULineRepo.findAllByHeaderId(header.getId())
                        .stream()
                        .filter(t -> t.getMeteringPoint().getVoltageClass().equals(meteringPoint.getVoltageClass()))
                        .map(t -> t.getMeteringPoint())
                        .findFirst()
                        .orElse(meteringPoint);

                    Double uAvg = UavgExpression.builder()
                        .meteringPointCode(inputMp.getCode())
                        .def(inputMp.getVoltageClass().getValue() / 1000d)
                        .context(context)
                        .service(resultUavgService)
                        .build()
                        .doubleValue();

                    for (MeterHistory meterHistory : meterHistories) {
                        BalanceSubstResultUbLine line = new BalanceSubstResultUbLine();

                        TtType ttType = meterHistory.getTtType();
                        TnType tnType = meterHistory.getTnType();
                        EemType eemType = meterHistory.getMeter().getEemType();

                        String paramCode = direction.equals("1") ? "A+" : "A-";
                        Double w = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                            .filter(t -> t.getParam().getCode().equals(paramCode))
                            .filter(t -> t.getMeterHistory() != null)
                            .filter(t -> t.getMeterHistory().equals(meterHistory))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);

                        Double wa = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                            .filter(t -> t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-"))
                            .filter(t -> t.getMeterHistory() != null)
                            .filter(t -> t.getMeterHistory().equals(meterHistory))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);

                        Double wr = mrLines.stream()
                            .filter(t -> !t.getIsIgnore())
                            .filter(t -> t.getMeteringPoint().equals(meteringPoint))
                            .filter(t -> t.getParam().getCode().equals("R+") || t.getParam().getCode().equals("R-"))
                            .filter(t -> t.getMeterHistory() != null)
                            .filter(t -> t.getMeterHistory().equals(meterHistory))
                            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                            .reduce((t1, t2) -> t1 + t2)
                            .orElse(0d);

                        Double i1avgVal = Math.sqrt(Math.pow(wa, 2) + Math.pow(wr, 2)) / ( 1.73d * uAvg * workHours);
                        Double i1avgProc = i1avgVal / ttType.getRatedCurrent1() * 100d;
                        Double ttAcProc = ttType.getAccuracyClass().getValue();

                        Double bttProc;
                        if      (i1avgProc > 100) bttProc = ttAcProc;
                        else if (i1avgProc >= 20) bttProc = 0.8125 - 0.003125 * i1avgProc;
                        else if (i1avgProc >= 5)  bttProc = 1.75 - 0.05 * i1avgProc;
                        else                      bttProc = 1.5;

                        Double biProc = Math.sqrt(2d) * bttProc;
                        Double buProc = tnType == null || tnType.getAccuracyClass() == null ? 0d : tnType.getAccuracyClass().getValue();
                        Double blProc = buProc <= 0.5 ? 0.25 : 0.5;
                        Double bsoProc = eemType == null || eemType.getAccuracyClass() == null ? 0d : eemType.getAccuracyClass().getValue();
                        Double bProc =  Math.sqrt(Math.pow(biProc, 2) + Math.pow(buProc, 2) + Math.pow(blProc, 2) + Math.pow(bsoProc, 2)) * 1.1d;

                        Double dol = 0d;
                        if (direction.equals("1")) dol = w / wSum1;
                        if (direction.equals("2")) dol = w / wSum2;

                        Double b2dol2 = Math.pow(bProc/100d, 2) * Math.pow(dol, 2);

                        line.setHeader(header);
                        line.setMeteringPoint(meteringPoint);
                        line.setDirection(direction);
                        line.setW(w);
                        line.setWa(wa);
                        line.setWr(wr);
                        line.setTtStar(null);
                        line.setTtacProc(ttAcProc);
                        line.setI1Nom(ttType.getRatedCurrent1());
                        line.setTRab(workHours);
                        line.setUavg(uAvg);
                        line.setI1avgVal(i1avgVal);
                        line.setI1avgProc(i1avgProc);
                        line.setBttFactor(null);
                        line.setBttProc(bttProc);
                        line.setBiProc(biProc);
                        line.setBuProc(buProc);
                        line.setBlProc(blProc);
                        line.setBsoProc(bsoProc);
                        line.setBProc(bProc);
                        line.setDol(dol);
                        line.setB2dol2(b2dol2);
                        lines.add(line);
                    }
                }
            }

            Double r1SumW = lines.stream()
                .filter(t -> t.getDirection().equals("1"))
                .filter(t -> t.getW() != null)
                .map(t -> t.getW())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r1SumB2D2 = lines.stream()
                .filter(t -> t.getDirection().equals("1"))
                .filter(t -> t.getB2dol2() != null)
                .map(t -> t.getB2dol2())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r2SumW = lines.stream()
                .filter(t -> t.getDirection().equals("2"))
                .filter(t -> t.getW() != null)
                .map(t -> t.getW())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r2SumB2D2 = lines.stream()
                .filter(t -> t.getDirection().equals("2"))
                .filter(t -> t.getB2dol2() != null)
                .map(t -> t.getB2dol2())
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double nbdProc = Math.sqrt(r1SumB2D2 + r2SumB2D2) * 100d;
            Double nbdVal = nbdProc * r1SumW / 100d;

            header.setNbdProc(nbdProc);
            header.setNbdVal(nbdVal);

            balanceSubstResultUbLineRepo.save(lines);
            balanceSubstResultHeaderRepo.save(header);
            updateStatus(header, BatchStatusEnum.C);
            logger.info("Unbalance for header " + header.getId() + " completed");
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Unbalance for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultUbLine> lines = balanceSubstResultUbLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultUbLineRepo.delete(lines.get(i));
        balanceSubstResultUbLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }
}
