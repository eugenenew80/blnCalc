package calc.formula.doc.impl;

import calc.entity.calc.BalanceSubstResultHeader;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceSubstUbServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstUbServiceImpl.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;

    public void calc(BalanceSubstResultHeader header)  {
        try {
            logger.info("Unbalance for header " + header.getId() + " started");
            updateStatus(header, BatchStatusEnum.P);
            header = balanceSubstResultHeaderRepo.findOne(header.getId());
            calcValues(header);
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

    private void calcValues(BalanceSubstResultHeader header) {
        balanceSubstResultHeaderRepo.calcUnbalance(header.getId());
    }
}
