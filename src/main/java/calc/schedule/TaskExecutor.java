package calc.schedule;

import calc.entity.calc.asp.AspResultHeaderW;
import calc.entity.calc.bs.BalanceSubstResultHeaderW;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.inter.InterResultHeaderW;
import calc.entity.calc.seg.SegResultHeaderW;
import calc.entity.calc.svr.SvrHeader;
import calc.formula.calculation.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
    private final BalanceSubstResultHeaderWRepo balanceSubstResultHeaderWRepo;
    private final InterResultHeaderWRepo interResultHeaderWRepo;
    private final BalanceSubstService balanceSubstService;
    private final AspResultHeaderWRepo aspResultHeaderWRepo;
    private final SegResultHeaderWRepo segResultHeaderWRepo;
    private final SvrHeaderRepo svrHeaderRepo;
    private final AspService aspService;
    private final InterService interService;
    private final SvrService svrService;

    @Scheduled(cron = "*/5 * * * * *")
    public void run() {
        calcBs();
        calcAsp();
        calcSvr();
        calcSeg();
        calcInter();
    }

    private void calcBs() {
        List<BalanceSubstResultHeaderW> headers = balanceSubstResultHeaderWRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("Расяёт балансов по подстанции, количество документов: " + headers.size());
        for (BalanceSubstResultHeaderW h : headers) {
            logger.info("Header " + h.getId() + " started");
            balanceSubstService.calc(h.getId());
            logger.info("Header " + h.getId() + " completed");
        }
    }

    private void calcAsp() {
        List<AspResultHeaderW> headers = aspResultHeaderWRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("Расчёт актов съёма показаний ПКУ, количество документов: " + headers.size());
        for (AspResultHeaderW header : headers) {
            logger.info("Header " + header.getId() + " started");
            aspService.calc(header.getId());
            logger.info("Header " + header.getId() + " completed");
        }
    }

    private void calcSeg() {
        List<SegResultHeaderW> headers = segResultHeaderWRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("Расчет баланса по сегменту сети, количество документов: " + headers.size());
        for (SegResultHeaderW header : headers) {
            logger.info("Header " + header.getId() + " started");
            aspService.calc(header.getId());
            logger.info("Header " + header.getId() + " completed");
        }
    }

    private void calcInter() {
        List<InterResultHeaderW> headers = interResultHeaderWRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("Расчет актов МГЛЭП, количество документов: " + headers.size());
        for (InterResultHeaderW header : headers) {
            logger.info("Header " + header.getId() + " started");
            interService.calc(header.getId());
            logger.info("Header " + header.getId() + " completed");
        }
    }

    private void calcSvr() {
        List<SvrHeader> headers = svrHeaderRepo.findAllByStatus(BatchStatusEnum.W);
        if (headers.size()==0) return;

        logger.info("Расчёт актов сверки объемов оказанных услуг, количество документов: " + headers.size());
        for (SvrHeader header : headers) {
            logger.info("Header " + header.getId() + " started");
            svrService.calc(header);
            logger.info("Header " + header.getId() + " completed");
        }
    }
}
