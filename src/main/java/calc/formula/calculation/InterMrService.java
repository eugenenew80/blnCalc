package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.*;
import calc.entity.calc.inter.*;
import calc.formula.*;
import calc.formula.exception.*;
import calc.formula.service.*;
import calc.repo.calc.InterResultMrLineRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

import static calc.util.Util.*;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class InterMrService {
    private static final Logger logger = LoggerFactory.getLogger(InterMrService.class);
    private static final String docCode = "INTER_MR";
    private final InterResultMrLineRepo interResultMrLineRepo;
    private final MrService mrService;
    private final MessageService messageService;

    public boolean calc(InterResultHeader header) {
        try {
            logger.info("started, headerId: " + header.getId());

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.DEFAULT)
                .build();

            List<MeteringPoint> points = header.getHeader().getLines()
                .stream()
                .flatMap(t -> Arrays.asList(t.getMeteringPoint1(), t.getMeteringPoint2(), t.getBoundMeteringPoint()).stream())
                .filter(t -> t != null)
                .filter(t -> t.getPointType() == PointTypeEnum.PMP)
                .distinct()
                .collect(toList());

            List<InterResultMrLine> resultLines = new ArrayList<>();
            for (MeteringPoint meteringPoint : points) {
                Map<String, String> msgParams = buildMsgParams(meteringPoint);
                List<MeteringReading> meteringReadings;
                try {
                    meteringReadings = mrService.calc(meteringPoint, context);
                }
                catch (CalcServiceException e) {
                    e.printStackTrace();
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, null, docCode, e.getErrCode(), msgParams);
                    continue;
                }

                for (MeteringReading t : meteringReadings) {
                    if (!(t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-")))
                        continue;

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
                    resultLine.setVal(round(t.getVal(), t.getParam()));
                    resultLine.setUnderCountVal(t.getUnderCountVal());
                    resultLine.setUndercount(t.getUnderCount());
                    resultLines.add(resultLine);
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

            messageService.addMessage(header, null, docCode, "RUNTIME_EXCEPTION", buildMsgParams(e));
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
        interResultMrLineRepo.delete(lines);
        interResultMrLineRepo.flush();
    }
}
