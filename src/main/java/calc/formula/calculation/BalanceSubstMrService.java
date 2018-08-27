package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.service.MeteringReading;
import calc.formula.service.MeteringReadingService;
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
public class BalanceSubstMrService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstMrService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final MeteringReadingService meteringReadingService;
    private static final String docCode = "ACT";

    public void calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Metering reading for header " + header.getId() + " started");
            header = balanceSubstResultHeaderRepo.findOne(header.getId());
            if (header.getStatus() == BatchStatusEnum.E)
                return;

            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .docId(header.getId())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
            for (BalanceSubstMrLine mrLine : header.getHeader().getMrLines()) {
                List<MeteringReading> meteringReadings = meteringReadingService.calc(mrLine.getMeteringPoint(), context);

                for (MeteringReading t : meteringReadings) {
                    BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();
                    line.setHeader(header);
                    line.setMeteringPoint(mrLine.getMeteringPoint());
                    line.setSection(getSection(mrLine));
                    line.setBypassMeteringPoint(t.getBypassMeteringPoint());
                    line.setBypassMode(t.getBypassMode());
                    line.setIsBypassSection(t.getIsBypassSection());
                    line.setIsIgnore(false);
                    line.setParam(t.getParam());
                    line.setUnit(t.getUnit());
                    line.setMeter(t.getMeter());
                    line.setMeterHistory(t.getMeterHistory());
                    line.setStartMeteringDate(t.getStartMeteringDate());
                    line.setEndMeteringDate(t.getEndMeteringDate());
                    line.setStartVal(t.getStartVal());
                    line.setEndVal(t.getEndVal());
                    line.setDelta(t.getDelta());
                    line.setMeterRate(t.getMeterRate());
                    line.setVal(t.getVal());
                    line.setUnderCountVal(t.getUnderCountVal());
                    line.setUndercount(t.getUnderCount());
                    resultLines.add(line);
                }
            }

            balanceSubstResultMrLineRepo.save(resultLines);
            updateStatus(header, BatchStatusEnum.C);
            logger.info("Metering reading for header " + header.getId() + " completed");
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMrLine> lines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultMrLineRepo.delete(lines.get(i));
        balanceSubstResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
    }

    private String getSection(BalanceSubstMrLine mrLine) {
        if (mrLine.getIsSection1()) return "1";
        if (mrLine.getIsSection2()) return "2";
        if (mrLine.getIsSection3()) return "3";
        if (mrLine.getIsSection4()) return "4";
        if (mrLine.getIsSection5()) return "5";
        return "";
    }
}
