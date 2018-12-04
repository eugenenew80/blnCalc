package calc.formula.calculation;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import calc.entity.calc.inter.InterResultHeader;
import calc.formula.service.MessageService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("ImplicitSubclassInspection")
@Service
@RequiredArgsConstructor
public class InterService {
    private static final Logger logger = LoggerFactory.getLogger(InterService.class);
    private final InterMrService interMrService;
    private final InterLineService interLineService;
    private final InterResultHeaderRepo interResultHeaderRepo;
    private final MessageService messageService;

    public void calc(Long headerId) {
        logger.info("Act inter with headerId " + headerId + " started");
        InterResultHeader header = interResultHeaderRepo.findOne(headerId);
        if (header.getStatus() != BatchStatusEnum.W)
            return;

        if (header.getDataType() == null)
            header.setDataType(header.getPeriodType() == PeriodTypeEnum.M ? DataTypeEnum.FINAL : DataTypeEnum.OPER);

        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteMessages(header);

            if (!interMrService.calc(header)) {
                updateStatus(header, BatchStatusEnum.E);
                return;
            }

            if (!interLineService.calc(header)) {
                updateStatus(header, BatchStatusEnum.E);
                return;
            }
            updateStatus(header, BatchStatusEnum.C);
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateStatus(InterResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        interResultHeaderRepo.save(header);
        interResultHeaderRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(InterResultHeader header) {
        messageService.deleteMessages(header);
    }
}
