package calc.formula.calculation;

import calc.entity.calc.Parameter;
import calc.entity.calc.bs.*;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
import calc.formula.service.MessageService;
import calc.formula.service.ParamService;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import calc.repo.calc.BalanceSubstResultLineRepo;
import calc.repo.calc.PowerTransformerValueRepo;
import calc.repo.calc.ReactorValueRepo;
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
    private static final String docCode = "BALANCE";
    private final BalanceSubstMrService balanceSubstMrService;
    private final BalanceSubstUbService balanceSubstUbService;
    private final BalanceSubstUService balanceSubstUService;
    private final BalanceSubstReactorService balanceSubstReactorService;
    private final BalanceSubstTransformerService balanceSubstTransformerService;
    private final BalanceSubstLineService balanceSubstLineService;
    private final MessageService messageService;
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultLineRepo balanceSubstResultLineRepo;
    private final ParamService paramService;
    private final PowerTransformerValueRepo powerTransformerValueRepo;
    private final ReactorValueRepo reactorValueRepo;

    public void calc(Long headerId) {
        logger.info("Balance for substation with headerId " + headerId + " started");
        BalanceSubstResultHeader header = balanceSubstResultHeaderRepo.findOne(headerId);
        BalanceSubstHeader templateHeader = header.getHeader();

        if (header.getStatus() != BatchStatusEnum.W)
            return;

        try {
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

            logger.trace("totals calculation");

            List<BalanceSubstResultLine> resultLines = balanceSubstResultLineRepo.findAllByHeaderId(header.getId());
            Double total1 = getTotal(resultLines, "1");
            Double total2 = getTotal(resultLines, "2");
            Double total3 = getTotal(resultLines, "3");
            Double total4 = getTotal(resultLines, "4");

            Double total5 = powerTransformerValueRepo.findAllByHeaderId(header.getId())
                .stream()
                .filter(t -> t.getIsBalance())
                .map(t -> Optional.ofNullable(t.getVal()).orElse(0d))
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double total6 = reactorValueRepo.findAllByHeaderId(header.getId())
                .stream()
                .filter(t -> t.getIsBalance())
                .map(t -> Optional.ofNullable(t.getVal()).orElse(0d))
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Parameter parAp = paramService.getValues().get("A+");
            if (parAp != null) {
                double rounding =  Math.pow(10, Optional.ofNullable(parAp.getDigitsRounding()).orElse(0));
                if (total1 != null) total1 = Math.round(total1 * rounding) / rounding;
            }

            Parameter parAm = paramService.getValues().get("A-");
            if (parAm != null) {
                double rounding =  Math.pow(10, Optional.ofNullable(parAm.getDigitsRounding()).orElse(0));
                if (total2 != null) total2 = Math.round(total2 * rounding) / rounding;
                if (total2 != null) total3 = Math.round(total3 * rounding) / rounding;
                if (total4 != null) total4 = Math.round(total4 * rounding) / rounding;
            }

            Parameter parWl = paramService.getValues().get("WL");
            if (parWl != null) {
                double rounding =  Math.pow(10, Optional.ofNullable(parWl.getDigitsRounding()).orElse(0));
                if (total5 != null) total5 = Math.round(total5 * rounding) / rounding;
                if (total6 != null) total6 = Math.round(total6 * rounding) / rounding;
            }

            Double lossFact = total1 - total3 - total4;
            Double nbfVal   = total1 - total2 - total3 - total4 - total5 - total6;
            Double nbfProc  = total1 != 0d ? 100d * nbfVal / total1 :  0d;

            Double nbdVal    = Optional.ofNullable(header.getNbdVal()).orElse(0d);
            Double nbDifVal  = Math.abs(nbfVal) - Math.abs(nbdVal);
            if (nbDifVal < 0) nbDifVal = 0d;
            Double nbDifProc = nbdVal != 0d ? 100d * nbDifVal / nbdVal : 0d;

            if (templateHeader.getMeteringPoint1() == null) messageService.addMessage(header, null, docCode, "BS_MP_SECTION1_NOT_FOUND", "раздел 1");
            if (templateHeader.getMeteringPoint2() == null) messageService.addMessage(header, null, docCode, "BS_MP_SECTION2_NOT_FOUND", "раздел 2");
            if (templateHeader.getMeteringPoint3() == null) messageService.addMessage(header, null, docCode, "BS_MP_SECTION3_NOT_FOUND", "раздел 3");
            if (templateHeader.getMeteringPoint4() == null) messageService.addMessage(header, null, docCode, "BS_MP_SECTION4_NOT_FOUND", "раздел 4");

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);
            header.setDataType(DataTypeEnum.OPER);

            header.setMeteringPoint1(templateHeader.getMeteringPoint1());
            header.setMeteringPoint2(templateHeader.getMeteringPoint2());
            header.setMeteringPoint3(templateHeader.getMeteringPoint3());
            header.setMeteringPoint4(templateHeader.getMeteringPoint4());
            header.setLossFactMeteringPoint(templateHeader.getLossFactMeteringPoint());
            header.setTotal1(total1);
            header.setTotal2(total2);
            header.setTotal3(total3);
            header.setTotal4(total4);
            header.setTotal5(total5);
            header.setTotal6(total6);
            header.setLossFact(lossFact);
            header.setNbfVal(nbfVal);
            header.setNbfProc(nbfProc);
            header.setNbDifVal(nbDifVal);
            header.setNbfProc(nbDifProc);

            updateStatus(header, BatchStatusEnum.C);
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", e.getClass().getCanonicalName());
            updateStatus(header, BatchStatusEnum.E);
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Double getTotal(List<BalanceSubstResultLine> resultLines, String section) {
        return resultLines.stream()
            .filter(t -> t.getSection() != null)
            .filter(t -> t!=null)
            .filter(t -> t.getVal() != null)
            .filter(t -> t.getSection().equals(section))
            .map(t -> Optional.ofNullable(t.getVal()).orElse(0d) )
            .reduce((t1, t2) -> t1 + t2)
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
