package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.formula.CalcContext;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.PeriodTimeValueService;
import calc.repo.calc.MeteringPointSettingRepo;
import calc.repo.calc.SvrHeaderRepo;
import calc.repo.calc.SvrLineRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;


@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
public class SvrService {
    private static final Logger logger = LoggerFactory.getLogger(SvrService.class);
    private final SvrHeaderRepo svrHeaderRepo;
    private final SvrLineRepo svrLineRepo;
    private final MeteringPointSettingRepo meteringPointSettingRepo;
    private final PeriodTimeValueService periodTimeValueService;
    private static final String docCode = "SVR";

    public boolean calc(SvrHeader header) {
        try {
            logger.info("Service value reconcilation for header " + header.getId() + " started");
            header = svrHeaderRepo.findOne(header.getId());
            if (header.getStatus() == BatchStatusEnum.E)
                return false;

            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .docId(header.getId())
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .isAsp(true)
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            List<MeteringPointSetting> lines = meteringPointSettingRepo.findAllByContractIdAndDate(
                header.getContract().getId(),
                header.getStartDate(),
                header.getEndDate()
            );

            List<SvrLine> resultLines = new ArrayList<>();
            for (MeteringPointSetting mps : lines) {
                MeteringPoint meteringPoint = mps.getMeteringPoint();

                PeriodTimeValueExpression ap = PeriodTimeValueExpression.builder()
                    .meteringPointCode(meteringPoint.getCode())
                    .parameterCode("A+")
                    .periodType(context.getPeriodType())
                    .rate(1d)
                    .startHour((byte) 0)
                    .endHour((byte) 23)
                    .service(periodTimeValueService)
                    .context(context)
                    .build();

                PeriodTimeValueExpression am = PeriodTimeValueExpression.builder()
                    .meteringPointCode(meteringPoint.getCode())
                    .parameterCode("A-")
                    .periodType(context.getPeriodType())
                    .rate(1d)
                    .startHour((byte) 0)
                    .endHour((byte) 23)
                    .service(periodTimeValueService)
                    .context(context)
                    .build();

                Double val = Math.abs(Optional.of(ap.doubleValue()).orElse(0d) - Optional.of(am.doubleValue()).orElse(0d));

                SvrLine line = new SvrLine();
                line.setHeader(header);
                line.setMeteringPoint(mps.getMeteringPoint());
                line.setTypeCode(mps.getTypeCode());
                line.setVal(val);

                resultLines.add(line);
                saveLines(resultLines);

            }
            updateStatus(header, BatchStatusEnum.C);
            logger.info("Metering reading for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Metering reading for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLines(List<SvrLine> lines) {
        svrLineRepo.save(lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteLines(SvrHeader header) {
        List<SvrLine> lines = svrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            svrLineRepo.delete(lines.get(i));
        svrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(SvrHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        svrHeaderRepo.save(header);
    }
}