package calc.formula.calculation;

import calc.entity.calc.bs.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
import calc.formula.service.MessageService;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import calc.repo.calc.BalanceSubstResultLineRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceSubstService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstService.class);
    private final BalanceSubstMrService balanceSubstMrService;
    private final BalanceSubstUbService balanceSubstUbService;
    private final BalanceSubstUService balanceSubstUService;
    private final BalanceSubstReactorService balanceSubstReactorService;
    private final BalanceSubstTransformerService balanceSubstTransformerService;
    private final BalanceSubstLineService balanceSubstLineService;
    private final MessageService messageService;
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultLineRepo balanceSubstResultLineRepo;
    private static final String docCode = "BALANCE";

    public void calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Balance for substation with headerId " + header.getId() + " started");
            header = balanceSubstResultHeaderRepo.findOne(header.getId());
            if (header.getStatus() != BatchStatusEnum.W)
                return;

            updateStatus(header, BatchStatusEnum.P);
            deleteMessages(header);

            if (!balanceSubstMrService.calc(header)) {
                updateStatus(header, BatchStatusEnum.E);
                return;
            }

            if (!balanceSubstUbService.calc(header)) {
                updateStatus(header, BatchStatusEnum.E);
                return;
            }

            if (!balanceSubstUService.calc(header)) {
                updateStatus(header, BatchStatusEnum.E);
                return;
            }

            if (!balanceSubstReactorService.calc(header)) {
                updateStatus(header, BatchStatusEnum.E);
                return;
            }

            if (!balanceSubstTransformerService.calc(header)) {
                updateStatus(header, BatchStatusEnum.E);
                return;
            }

            if (!balanceSubstLineService.calc(header)) {
                updateStatus(header, BatchStatusEnum.E);
                return;
            }

            List<BalanceSubstResultLine> resultLines = balanceSubstResultLineRepo.findAllByHeaderId(header.getId());
            Double total1 = getTotal(resultLines, "1");
            Double total2 = getTotal(resultLines, "2");
            Double total3 = getTotal(resultLines, "3");
            Double total4 = getTotal(resultLines, "4");
            Double lossFact = total1 - total2 - total3 - total4;

            if (header.getHeader().getMeteringPoint1() == null) messageService.addMessage(header, null, docCode, "BS_MP_SECTION1_NOT_FOUND");
            if (header.getHeader().getMeteringPoint2() == null) messageService.addMessage(header, null, docCode, "BS_MP_SECTION2_NOT_FOUND");
            if (header.getHeader().getMeteringPoint3() == null) messageService.addMessage(header, null, docCode, "BS_MP_SECTION3_NOT_FOUND");
            if (header.getHeader().getMeteringPoint4() == null) messageService.addMessage(header, null, docCode, "BS_MP_SECTION4_NOT_FOUND");

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);
            header.setDataType(DataTypeEnum.OPER);
            header.setMeteringPoint1(header.getHeader().getMeteringPoint1());
            header.setMeteringPoint2(header.getHeader().getMeteringPoint2());
            header.setMeteringPoint3(header.getHeader().getMeteringPoint3());
            header.setMeteringPoint4(header.getHeader().getMeteringPoint4());
            header.setLossFactMeteringPoint(header.getHeader().getLossFactMeteringPoint());
            header.setTotal1(total1);
            header.setTotal2(total2);
            header.setTotal3(total3);
            header.setTotal4(total4);
            header.setLossFact(lossFact);

            updateStatus(header, BatchStatusEnum.C);
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION");
            updateStatus(header, BatchStatusEnum.E);
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Double getTotal(List<BalanceSubstResultLine> resultLines, String section) {
        return resultLines.stream()
            .filter(t -> t.getSection().equals(section))
            .map(t -> t.getVal())
            .reduce((t1, t2) -> Optional.ofNullable(t1).orElse(0d) + Optional.ofNullable(t2).orElse(0d))
            .orElse(0d);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteMessages(BalanceSubstResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateStatus(BalanceSubstResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        balanceSubstResultHeaderRepo.save(header);
        balanceSubstResultHeaderRepo.flush();
    }
}
