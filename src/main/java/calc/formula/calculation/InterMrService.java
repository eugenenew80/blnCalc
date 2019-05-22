package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.*;
import calc.entity.calc.inter.*;
import calc.formula.*;
import calc.formula.exception.*;
import calc.formula.service.*;
import calc.repo.calc.BypassModeRepo;
import calc.repo.calc.InterResultMrLineRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final BypassModeRepo bypassModeRepo;

    public boolean calc(InterResultHeader header) {
        try {
            logger.info("started, headerId: " + header.getId());

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.DEFAULT)
                .build();


            LocalDateTime startDate = context.getHeader().getStartDate().atStartOfDay();
            LocalDateTime endDate = context.getHeader().getEndDate().atStartOfDay().plusDays(1);

            List<InterResultMrLine> resultLines = new ArrayList<>();

            //Display all modes - side 1
            for (InterLine line : header.getHeader().getLines()) {
                if (!line.getIsDisplayAllModesBypass1())
                    continue;

                MeteringPoint bypassMeteringPoint = line.getMeteringPointBypass1();
                MeteringPoint meteringPoint = line.getMeteringPoint1();

                List<BypassMode> bypassModes = bypassModeRepo.findAllByBypass(bypassMeteringPoint.getId(), startDate, endDate);

                if (bypassModes.isEmpty()) {
                    List<MeteringReading> meteringReadings = mrService.calc(bypassMeteringPoint, context);

                    for (MeteringReading t : meteringReadings) {
                        if (!(t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-")))
                            continue;

                        InterResultMrLine resultLine = new InterResultMrLine();
                        resultLine.setHeader(header);
                        resultLine.setMeteringPoint(meteringPoint);
                        resultLine.setBypassMeteringPoint(bypassMeteringPoint);
                        resultLine.setIsBypassSection(t.getIsBypassSection());
                        resultLine.setParam(t.getParam());
                        resultLine.setUnit(t.getUnit());
                        resultLine.setStartMeteringDate(t.getStartMeteringDate());
                        resultLine.setEndMeteringDate(t.getEndMeteringDate());
                        resultLine.setStartVal(t.getStartVal());
                        resultLine.setEndVal(t.getEndVal());
                        resultLine.setDelta(t.getDelta());
                        resultLine.setMeterRate(t.getMeterRate());
                        resultLine.setVal(round(t.getVal(), t.getParam()));
                        resultLine.setUnderCountVal(t.getUnderCountVal());
                        resultLine.setUndercount(t.getUnderCount());
                        resultLine.setIsForTotal(false);
                        resultLines.add(resultLine);
                    }
                }

                for (BypassMode bypassMode : bypassModes) {
                    List<MeteringReading> meteringReadings = mrService.calc(bypassMode.getMeteringPoint(), context);

                    for (MeteringReading t : meteringReadings) {
                        if (!(t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-")))
                            continue;

                        if (t.getBypassMeteringPoint() == null)
                            continue;

                        if (t.getMeteringPoint().equals(meteringPoint))
                            continue;

                        if (!t.getBypassMeteringPoint().equals(bypassMeteringPoint))
                            continue;

                        InterResultMrLine resultLine = new InterResultMrLine();
                        resultLine.setHeader(header);
                        resultLine.setMeteringPoint(meteringPoint);
                        resultLine.setBypassMeteringPoint(bypassMeteringPoint);
                        resultLine.setBypassMode(t.getBypassMode());
                        resultLine.setIsBypassSection(t.getIsBypassSection());
                        resultLine.setParam(t.getParam());
                        resultLine.setUnit(t.getUnit());
                        resultLine.setStartMeteringDate(t.getStartMeteringDate());
                        resultLine.setEndMeteringDate(t.getEndMeteringDate());
                        resultLine.setStartVal(t.getStartVal());
                        resultLine.setEndVal(t.getEndVal());
                        resultLine.setDelta(t.getDelta());
                        resultLine.setMeterRate(t.getMeterRate());
                        resultLine.setVal(round(t.getVal(), t.getParam()));
                        resultLine.setUnderCountVal(t.getUnderCountVal());
                        resultLine.setUndercount(t.getUnderCount());
                        resultLine.setIsForTotal(false);
                        resultLines.add(resultLine);
                    }
                }
            }


            //Display all modes - side 2
            for (InterLine line : header.getHeader().getLines()) {
                if (!line.getIsDisplayAllModesBypass2())
                    continue;

                MeteringPoint bypassMeteringPoint = line.getMeteringPointBypass2();
                MeteringPoint meteringPoint = line.getMeteringPoint2();

                List<BypassMode> bypassModes = bypassModeRepo.findAllByBypass(bypassMeteringPoint.getId(), startDate, endDate);

                if (bypassModes.isEmpty()) {
                    List<MeteringReading> meteringReadings = mrService.calc(bypassMeteringPoint, context);

                    for (MeteringReading t : meteringReadings) {
                        if (!(t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-")))
                            continue;

                        InterResultMrLine resultLine = new InterResultMrLine();
                        resultLine.setHeader(header);
                        resultLine.setMeteringPoint(meteringPoint);
                        resultLine.setBypassMeteringPoint(bypassMeteringPoint);
                        resultLine.setIsBypassSection(t.getIsBypassSection());
                        resultLine.setParam(t.getParam());
                        resultLine.setUnit(t.getUnit());
                        resultLine.setStartMeteringDate(t.getStartMeteringDate());
                        resultLine.setEndMeteringDate(t.getEndMeteringDate());
                        resultLine.setStartVal(t.getStartVal());
                        resultLine.setEndVal(t.getEndVal());
                        resultLine.setDelta(t.getDelta());
                        resultLine.setMeterRate(t.getMeterRate());
                        resultLine.setVal(round(t.getVal(), t.getParam()));
                        resultLine.setUnderCountVal(t.getUnderCountVal());
                        resultLine.setUndercount(t.getUnderCount());
                        resultLine.setIsForTotal(false);
                        resultLines.add(resultLine);
                    }
                }

                for (BypassMode bypassMode : bypassModes) {
                    List<MeteringReading> meteringReadings = mrService.calc(bypassMode.getMeteringPoint(), context);

                    for (MeteringReading t : meteringReadings) {
                        if (!(t.getParam().getCode().equals("A+") || t.getParam().getCode().equals("A-")))
                            continue;

                        if (t.getBypassMeteringPoint() == null)
                            continue;

                        if (t.getMeteringPoint().equals(meteringPoint))
                            continue;

                        if (!t.getBypassMeteringPoint().equals(bypassMeteringPoint))
                            continue;

                        InterResultMrLine resultLine = new InterResultMrLine();
                        resultLine.setHeader(header);
                        resultLine.setMeteringPoint(meteringPoint);
                        resultLine.setBypassMeteringPoint(bypassMeteringPoint);
                        resultLine.setBypassMode(t.getBypassMode());
                        resultLine.setIsBypassSection(t.getIsBypassSection());
                        resultLine.setParam(t.getParam());
                        resultLine.setUnit(t.getUnit());
                        resultLine.setStartMeteringDate(t.getStartMeteringDate());
                        resultLine.setEndMeteringDate(t.getEndMeteringDate());
                        resultLine.setStartVal(t.getStartVal());
                        resultLine.setEndVal(t.getEndVal());
                        resultLine.setDelta(t.getDelta());
                        resultLine.setMeterRate(t.getMeterRate());
                        resultLine.setVal(round(t.getVal(), t.getParam()));
                        resultLine.setUnderCountVal(t.getUnderCountVal());
                        resultLine.setUndercount(t.getUnderCount());
                        resultLine.setIsForTotal(false);
                        resultLines.add(resultLine);
                    }
                }
            }


            //Physical metering points list
            List<MeteringPoint> points = header.getHeader().getLines()
                .stream()
                .flatMap(t ->
                    Arrays.asList(
                        t.getDefMethodValue1() == DefMethodValue.DMV_FIXED_VALUE ? null : t.getMeteringPoint1(),
                        t.getDefMethodValue2() == DefMethodValue.DMV_FIXED_VALUE ? null : t.getMeteringPoint2(),
                        t.getBoundMeteringPoint()).stream()
                )
                .filter(t -> t != null)
                .filter(t -> t.getPointType() == PointTypeEnum.PMP)
                .distinct()
                .collect(toList());


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
                    resultLine.setIsForTotal(true);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void saveLines(List<InterResultMrLine> resultLines) {
        interResultMrLineRepo.save(resultLines);
        interResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void deleteLines(InterResultHeader header) {
        List<InterResultMrLine> lines = interResultMrLineRepo.findAllByHeaderId(header.getId());
        interResultMrLineRepo.delete(lines);
        interResultMrLineRepo.flush();
    }
}
