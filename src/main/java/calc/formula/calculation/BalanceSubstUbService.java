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
                Double i1avgVal = Math.sqrt(Math.pow(wa,2) + Math.pow(wr,2)) / (Math.sqrt(3)*ratedVoltage) ;


                for (MeterHistory meterHistory : meterHistories) {
                    meterHistory.getTtType().getAccuracyClass().getValue();
                    meterHistory.getTtType().getRatedCurrent1();

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
        return 24d;
    }
}
