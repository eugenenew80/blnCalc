package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import calc.repo.calc.BalanceSubstResultMrLineRepo;
import calc.repo.calc.MeterHistoryRepo;
import calc.repo.calc.MeteringPointModeRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class BalanceSubstUbService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstUbService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final MeteringPointModeRepo meteringPointModeRepo;

    public void calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("Unbalance for header " + header.getId() + " started");
            updateStatus(header, BatchStatusEnum.P);
            header = balanceSubstResultHeaderRepo.findOne(header.getId());

            CalcContext context = CalcContext.builder()
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .docCode("ACT")
                .docId(header.getId())
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();


            List<BalanceSubstResultMrLine> mrLines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());
            for (BalanceSubstUbLine ubLine : header.getHeader().getUbLines()) {
                BalanceSubstResultUbLine line = new BalanceSubstResultUbLine();

                Double wa = mrLines.stream()
                    .filter(t -> t.getMeteringPoint().equals(ubLine.getMeteringPoint()))
                    .filter(t -> t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-"))
                    .filter(t -> !t.getIsIgnore())
                    .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                    .reduce((t1, t2) -> t1 + t2)
                    .get();

                Double wr = mrLines.stream()
                    .filter(t -> t.getMeteringPoint().equals(ubLine.getMeteringPoint()))
                    .filter(t -> t.getParam().getCode().equals("R+") || t.getParam().getCode().equals("R-"))
                    .filter(t -> !t.getIsIgnore())
                    .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) + Optional.ofNullable(t.getUnderCountVal()).orElse(0d))
                    .reduce((t1, t2) -> t1 + t2)
                    .get();

                List<MeterHistory> meterHistories = mrLines.stream()
                    .filter(t -> t.getMeteringPoint().equals(ubLine.getMeteringPoint()))
                    .filter(t -> !t.getIsIgnore())
                    .filter(t -> t.getMeter() != null && t.getMeterHistory() != null)
                    .map(t -> t.getMeterHistory())
                    .distinct()
                    .collect(toList());

                Double workHours = getWorkHours(ubLine.getMeteringPoint(), context);
                Double ratedVoltage = ubLine.getMeteringPoint().getRatedVoltage();
                Double i1avgVal = Math.sqrt(Math.pow(wa,2) + Math.pow(wr,2)) / (Math.sqrt(3)*ratedVoltage);


                for (MeterHistory meterHistory : meterHistories) {
                    Double accuracyClass = meterHistory.getTtType().getAccuracyClass().getValue();
                    Double ratedCurrent1 = meterHistory.getTtType().getRatedCurrent1();
                    Double i1avgProc = i1avgVal / ratedCurrent1;

                    Double bttProc;
                    if (i1avgProc > 100) bttProc = accuracyClass;
                    else if (i1avgProc >= 20) bttProc = 0.8125 - 0.003125 * i1avgProc;
                    else if (i1avgProc >= 5) bttProc = 1.75 - 0.05 * i1avgProc;
                    else bttProc = 1.5;

                    Double biProc = Math.sqrt(2 * bttProc);

                    Double buProc;
                    if (meterHistory.getTnType() == null)
                        buProc = 0d;
                    else
                        buProc = meterHistory.getTnType().getAccuracyClass().getValue();

                    Double blProc;
                    if (buProc <= 0.5)
                        blProc = 0.25;
                    else
                        blProc = 0.5;

                    Double bsoProc = meterHistory.getMeter().getEemType().getAccuracyClass().getValue();
                    Double bProc = Math.pow(biProc, 2) + Math.pow(buProc, 2) + Math.pow(blProc, 2) + Math.pow(bsoProc, 2);
                }
            }


            updateStatus(header, BatchStatusEnum.C);
            logger.info("Unbalance for header " + header.getId() + " completed");
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Unbalance for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }

    private Double getWorkHours(MeteringPoint meteringPoint, CalcContext context) {
        LocalDateTime startDate = context.getStartDate().atStartOfDay();
        LocalDateTime endDate = context.getEndDate().atStartOfDay().plusDays(1);

        List<MeteringPointMode> modes = meteringPointModeRepo.findAllByMeteringPointIdAndDate(meteringPoint.getId(), startDate, endDate);
        Double hours = getHoursBetween(startDate, endDate);
        for (MeteringPointMode mode : modes) {
            LocalDateTime modeStartDate = Optional.ofNullable(mode.getStartDate()).orElse(LocalDateTime.MIN);
            LocalDateTime modeEndDate = Optional.ofNullable(mode.getEbdDate()).orElse(LocalDateTime.MAX);

            if (startDate.isAfter(modeStartDate))
                modeStartDate = startDate;

            if (modeEndDate.isAfter(endDate))
                modeEndDate = endDate;

            hours = hours - getHoursBetween(modeStartDate, modeEndDate);
        }

        return hours;
    }

    private Double getHoursBetween(LocalDateTime startDate, LocalDateTime endDate) {
        long hours = ChronoUnit.HOURS.between(startDate, endDate);
        long minutes = ChronoUnit.MINUTES.between(startDate, endDate)-60*hours;
        return Math.round((hours + minutes / 60d)*100d) / 100d;
    }
}
