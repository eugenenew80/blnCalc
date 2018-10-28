package calc.formula.calculation;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.inter.InterLine;
import calc.entity.calc.inter.InterResultHeader;
import calc.entity.calc.inter.InterResultMrLine;
import calc.formula.CalcContext;
import calc.formula.ContextType;
import calc.formula.service.MeteringReading;
import calc.formula.service.MrService;
import calc.repo.calc.InterResultMrLineRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InterMrService {
    private static final Logger logger = LoggerFactory.getLogger(InterMrService.class);
    private final InterResultMrLineRepo interResultMrLineRepo;
    private final MrService mrService;
    private static final String docCode = "INTER_MR";

    public boolean calc(InterResultHeader header) {
        try {
            logger.info("started, headerId: " + header.getId());

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .docCode(docCode)
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .contextType(ContextType.DEFAULT)
                .values(new HashMap<>())
                .build();

            List<InterResultMrLine> resultLines = new ArrayList<>();
            for (InterLine line : header.getHeader().getLines()) {
                for (MeteringPoint meteringPoint : Arrays.asList(line.getMeteringPoint1(), line.getMeteringPoint2(), line.getBoundMeteringPoint())) {
                    if (meteringPoint == null)
                        continue;

                    List<MeteringReading> meteringReadings = mrService.calc(meteringPoint, context);
                    for (MeteringReading t : meteringReadings) {
                        InterResultMrLine resultLine = new InterResultMrLine();
                        resultLine.setHeader(header);
                        resultLine.setMeteringPoint(meteringPoint);
                        resultLine.setBypassMeteringPoint(t.getBypassMeteringPoint());
                        resultLine.setBypassMode(t.getBypassMode());
                        resultLine.setIsBypassSection(t.getIsBypassSection());
                        resultLine.setParam(t.getParam());
                        resultLine.setUnit(t.getUnit());
                        resultLine.setMeter(t.getMeter());
                        resultLine.setMeterHistory(t.getMeterHistory());
                        resultLine.setStartMeteringDate(t.getStartMeteringDate());
                        resultLine.setEndMeteringDate(t.getEndMeteringDate());
                        resultLine.setStartVal(t.getStartVal());
                        resultLine.setEndVal(t.getEndVal());
                        resultLine.setDelta(t.getDelta());
                        resultLine.setMeterRate(t.getMeterRate());
                        resultLine.setVal(t.getVal());
                        resultLine.setUnderCountVal(t.getUnderCountVal());
                        resultLine.setUndercount(t.getUnderCount());
                        resultLines.add(resultLine);
                    }
                }
            }

            deleteLines(header);
            saveLines(resultLines);

            logger.info("completed, headerId: " + header.getId());
            return true;
        }

        catch (Exception e) {
            logger.error("terminated, headerId " + header.getId() + " , exception: " + e.toString() + ", " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<InterResultMrLine> resultLines) {
        interResultMrLineRepo.save(resultLines);
        interResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(InterResultHeader header) {
        List<InterResultMrLine> lines = interResultMrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            interResultMrLineRepo.delete(lines.get(i));
        interResultMrLineRepo.flush();
    }
}
