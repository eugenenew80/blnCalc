package calc.formula.calculation;

import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.source.SourceResultHeader;
import calc.formula.CalcContext;
import calc.formula.ContextType;
import calc.formula.service.CalcService;
import calc.formula.service.MessageService;
import calc.repo.calc.SourceResultHeaderRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;

@SuppressWarnings({"Duplicates", "ImplicitSubclassInspection"})
@Service
@RequiredArgsConstructor
public class SourceService {
    private static final Logger logger = LoggerFactory.getLogger(SourceService.class);
    private static final String docCode = "ES";
    private final SourceResultHeaderRepo sourceResultHeaderRepo;
    private final MessageService messageService;
    private final CalcService calcService;

    public boolean calc(Long headerId) {
        logger.info("Energy source balance for header " + headerId + " started");
        SourceResultHeader header = sourceResultHeaderRepo.findOne(headerId);
        if (header.getStatus() == BatchStatusEnum.E)
            return false;

        CalcContext context = CalcContext.builder()
            .lang(LangEnum.RU)
            .docCode(docCode)
            .headerId(header.getId())
            .periodType(header.getPeriodType())
            .startDate(header.getStartDate())
            .endDate(header.getEndDate())
            .orgId(header.getOrganization().getId())
            .contextType(ContextType.ES)
            .build();

        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteMessages(header);

            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);

            updateStatus(header, BatchStatusEnum.C);
            logger.info("Energy source balance for header " + header.getId() + " completed");

            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", new HashMap<>());
            updateStatus(header, BatchStatusEnum.E);
            logger.error("Energy source balance for header " + header.getId() + " terminated with exception");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteMessages(SourceResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateStatus(SourceResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        sourceResultHeaderRepo.save(header);
        sourceResultHeaderRepo.flush();
    }
}
