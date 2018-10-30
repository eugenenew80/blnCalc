package calc.formula.calculation;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.inter.*;
import calc.entity.calc.loss.*;
import calc.entity.calc.seg.SegResultHeader;
import calc.formula.CalcContext;
import calc.formula.ContextType;
import calc.formula.expression.impl.BinaryExpression;
import calc.formula.expression.impl.InterMrExpression;
import calc.formula.service.InterResultMrService;
import calc.formula.service.MessageService;
import calc.formula.service.OperatorFactory;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Optional.ofNullable;

@SuppressWarnings({"Duplicates", "ImplicitSubclassInspection"})
@Service
@RequiredArgsConstructor
public class LossFactService {
    private static final Logger logger = LoggerFactory.getLogger(LossFactService.class);
    private static final String docCode = "LOSS_FACT";
    private final LossFactResultHeaderRepo lossFactResultHeaderRepo;
    private final LossFactResultSec1LineRepo lossFactResultSec1LineRepo;
    private final LossFactResultSec2LineRepo lossFactResultSec2LineRepo;

    public boolean calc(Long headerId) {
        LossFactResultHeader header = lossFactResultHeaderRepo.findOne(headerId);
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
            .contextType(ContextType.DEFAULT)
            .values(new HashMap<>())
            .build();

        logger.info("started, headerId: " + header.getId());
        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);

            List<LossFactResultSec1Line> sec1Lines = calcSec1Lines(header, context);
            List<LossFactResultSec2Line> sec2Lines = calcSec2Lines(header, context);

            saveSec1Lines(sec1Lines);
            saveSec2Lines(sec2Lines);



            header.setLastUpdateDate(LocalDateTime.now());
            header.setIsActive(false);
            updateStatus(header, BatchStatusEnum.C);

            logger.info("completed, headerId: " + header.getId());
            return true;
        }

        catch (Exception e) {
            updateStatus(header, BatchStatusEnum.E);
            logger.error("terminated, headerId " + header.getId() + " , exception: " + e.toString() + ", " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<LossFactResultSec1Line> calcSec1Lines(LossFactResultHeader header, CalcContext context) {
        List<LossFactResultSec1Line> resultLines = new ArrayList<>();
        for (LossFactSec1Line line : header.getHeader().getLines1()) {
            LossFactResultSec1Line resultLine = new LossFactResultSec1Line();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setSectionCode(line.getSectionCode());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setCreateDate(LocalDateTime.now());
            resultLine.setCreateBy(header.getLastUpdateBy());

            resultLines.add(resultLine);
        }
        return resultLines;
    }

    private List<LossFactResultSec2Line> calcSec2Lines(LossFactResultHeader header, CalcContext context) {
        List<LossFactResultSec2Line> resultLines = new ArrayList<>();
        for (LossFactSec2Line line : header.getHeader().getLines2()) {
            LossFactResultSec2Line resultLine = new LossFactResultSec2Line();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setOrg(line.getOrg());
            resultLine.setMeteringPoint(line.getMeteringPoint());
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setCreateDate(LocalDateTime.now());
            resultLine.setCreateBy(header.getLastUpdateBy());

            resultLines.add(resultLine);
        }
        return resultLines;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveSec1Lines(List<LossFactResultSec1Line> resultLines) {
        lossFactResultSec1LineRepo.save(resultLines);
        lossFactResultSec1LineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveSec2Lines(List<LossFactResultSec2Line> resultLines) {
        lossFactResultSec2LineRepo.save(resultLines);
        lossFactResultSec2LineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(LossFactResultHeader header) {
        List<LossFactResultSec1Line> lines1 = lossFactResultSec1LineRepo.findAllByHeaderId(header.getId());
        lossFactResultSec1LineRepo.delete(lines1);
        lossFactResultSec1LineRepo.flush();

        List<LossFactResultSec2Line> lines2 = lossFactResultSec2LineRepo.findAllByHeaderId(header.getId());
        lossFactResultSec2LineRepo.delete(lines2);
        lossFactResultSec2LineRepo.flush();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateStatus(LossFactResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        lossFactResultHeaderRepo.save(header);
        lossFactResultHeaderRepo.flush();
    }

}
