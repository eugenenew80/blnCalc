package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.*;
import calc.entity.calc.loss.*;
import calc.formula.*;
import calc.formula.exception.CalcServiceException;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import static calc.util.Util.*;
import static java.util.Optional.*;

@SuppressWarnings({"ImplicitSubclassInspection"})
@Service
@RequiredArgsConstructor
public class LossFactService {
    private static final Logger logger = LoggerFactory.getLogger(LossFactService.class);
    private static final String docCode = "LOSS_FACT";
    private final LossFactResultHeaderRepo lossFactResultHeaderRepo;
    private final LossFactResultSec1LineRepo lossFactResultSec1LineRepo;
    private final LossFactResultSec2LineRepo lossFactResultSec2LineRepo;
    private final CalcService calcService;
    private final ParamService paramService;
    private final MessageService messageService;

    public boolean calc(Long headerId) {
        LossFactResultHeader header = lossFactResultHeaderRepo.findOne(headerId);
        if (header.getStatus() != BatchStatusEnum.W)
            return false;

        if (header.getDataType() == null)
            header.setDataType(header.getPeriodType() == PeriodTypeEnum.M ? DataTypeEnum.FINAL : DataTypeEnum.OPER);

        CalcContext context = CalcContext.builder()
            .lang(LangEnum.RU)
            .header(header)
            .defContextType(ContextTypeEnum.DEFAULT)
            .build();

        logger.info("started, headerId: " + header.getId());
        try {
            updateStatus(header, BatchStatusEnum.P);
            deleteLines(header);
            deleteMessages(header);

            List<LossFactResultSec1Line> sec1Lines = calcSec1Lines(header, context);
            List<LossFactResultSec2Line> sec2Lines = calcSec2Lines(header, context);

            saveSec1Lines(sec1Lines);
            saveSec2Lines(sec2Lines);

            MeteringPoint meteringPoint = header.getHeader().getMeteringPoint();
            Map<String, String> msgParams = buildMsgParams(meteringPoint);

            Double r321 = null;
            try {
                Parameter param = paramService.getParam("AB");
                r321 = getMrVal(meteringPoint, param, context);
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, null, docCode, e.getErrCode(), msgParams);
            }

            Stream<Pair<Double, Double>> aStream = Stream.concat(
                sec1Lines.stream().map(t -> Pair.of(ofNullable(t.getAp()).orElse(0d), ofNullable(t.getAm()).orElse(0d))),
                sec2Lines.stream().map(t -> Pair.of(ofNullable(t.getAp()).orElse(0d), ofNullable(t.getAm()).orElse(0d)))
            );
            Double totalAp = aStream.map(t -> t.getFirst()).reduce((t1, t2) -> t1 + t2).orElse(0d);

            aStream = Stream.concat(
                sec1Lines.stream().map(t -> Pair.of(ofNullable(t.getAp()).orElse(0d), ofNullable(t.getAm()).orElse(0d))),
                sec2Lines.stream().map(t -> Pair.of(ofNullable(t.getAp()).orElse(0d), ofNullable(t.getAm()).orElse(0d)))
            );
            Double totalAm = aStream.map(t -> t.getSecond()).reduce((t1, t2) -> t1 + t2).orElse(0d);

            Double lossFact = totalAp - totalAm;

            Double r322 = sec1Lines.stream()
                .filter(t -> t.getSectionCode().equals("1.1"))
                .map(t -> ofNullable(t.getAp()).orElse(0d))
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r323 = sec2Lines.stream()
                .map(t -> ofNullable(t.getAm()).orElse(0d))
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r324 = sec1Lines.stream()
                .filter(t -> t.getSectionCode().equals("1.3"))
                .map(t -> ofNullable(t.getAm()).orElse(0d) - ofNullable(t.getAp()).orElse(0d))
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            Double r32 = ofNullable(r321).orElse(0d) +  r322 + r323 + r324;
            Double r325 = r32 + totalAp;

            Double r326 = ofNullable(r325).orElse(0d) != 0
                ?  ofNullable(lossFact).orElse(0d) * 100 / ofNullable(r325).orElse(0d)
                : null;

            r326 = round(r326, 2);

            header.setAp(totalAp);
            header.setAm(totalAm);
            header.setLossFact(lossFact);
            header.setR321(r321);
            header.setR322(r322);
            header.setR323(r323);
            header.setR324(r324);
            header.setR325(r325);
            header.setR326(r326);
            header.setR32(r32);

            header.setLastUpdateDate(LocalDateTime.now());
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
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Map<String, String> msgParams = buildMsgParams(meteringPoint);

            Double ap = null, am = null;
            Parameter paramAp = inverseParam(paramService, paramService.getParam("A+"), line.getIsInverse());
            Parameter paramAm = inverseParam(paramService, paramService.getParam("A-"), line.getIsInverse());

            //noinspection Duplicates
            try {
                ap = getMrVal(meteringPoint, paramAp, context);
                am = getMrVal(meteringPoint, paramAm, context);
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
            }

            LossFactResultSec1Line resultLine = new LossFactResultSec1Line();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setSectionCode(line.getSectionCode());
            resultLine.setMeteringPoint(meteringPoint);
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setCreateDate(LocalDateTime.now());
            resultLine.setCreateBy(header.getLastUpdateBy());
            resultLine.setAp(ap);
            resultLine.setAm(am);
            resultLines.add(resultLine);
        }
        return resultLines;
    }

    private List<LossFactResultSec2Line> calcSec2Lines(LossFactResultHeader header, CalcContext context) {
        List<LossFactResultSec2Line> resultLines = new ArrayList<>();
        for (LossFactSec2Line line : header.getHeader().getLines2()) {
            MeteringPoint meteringPoint = line.getMeteringPoint();
            Map<String, String> msgParams = buildMsgParams(meteringPoint);

            Double ap = null; Double am = null;
            Parameter paramAp = inverseParam(paramService, paramService.getParam("A+"), line.getIsInverse());
            Parameter paramAm = inverseParam(paramService, paramService.getParam("A-"), line.getIsInverse());

            //noinspection Duplicates
            try {
                ap = getMrVal(meteringPoint, paramAp, context);
                am = getMrVal(meteringPoint, paramAm, context);
            }
            catch (CalcServiceException e) {
                msgParams.putIfAbsent("err", e.getMessage());
                messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
            }

            LossFactResultSec2Line resultLine = new LossFactResultSec2Line();
            resultLine.setHeader(header);
            resultLine.setLineNum(line.getLineNum());
            resultLine.setOrg(line.getOrg());
            resultLine.setMeteringPoint(meteringPoint);
            resultLine.setIsInverse(line.getIsInverse());
            resultLine.setCreateDate(LocalDateTime.now());
            resultLine.setCreateBy(header.getLastUpdateBy());
            resultLine.setAp(ap);
            resultLine.setAm(am);
            resultLines.add(resultLine);
        }
        return resultLines;
    }

    private Double getMrVal(MeteringPoint meteringPoint, Parameter param, CalcContext context) {
        if (meteringPoint == null || param == null)
            return null;

        CalcProperty property = CalcProperty.builder()
            .processOrder(ProcessOrderEnum.READ_CALC)
            .contextType(context.getDefContextType())
            .build();

        CalcResult result = calcService.calcValue(meteringPoint, param, context, property);
        Double val = result !=null ? result.getDoubleValue() : null;

        val = round(val, param);
        return val;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void saveSec1Lines(List<LossFactResultSec1Line> resultLines) {
        lossFactResultSec1LineRepo.save(resultLines);
        lossFactResultSec1LineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void saveSec2Lines(List<LossFactResultSec2Line> resultLines) {
        lossFactResultSec2LineRepo.save(resultLines);
        lossFactResultSec2LineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    void deleteLines(LossFactResultHeader header) {
        List<LossFactResultSec1Line> lines1 = lossFactResultSec1LineRepo.findAllByHeaderId(header.getId());
        lossFactResultSec1LineRepo.delete(lines1);
        lossFactResultSec1LineRepo.flush();

        List<LossFactResultSec2Line> lines2 = lossFactResultSec2LineRepo.findAllByHeaderId(header.getId());
        lossFactResultSec2LineRepo.delete(lines2);
        lossFactResultSec2LineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    public void deleteMessages(LossFactResultHeader header) {
        messageService.deleteMessages(header);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void updateStatus(LossFactResultHeader header, BatchStatusEnum status) {
        header.setStatus(status);
        lossFactResultHeaderRepo.save(header);
        lossFactResultHeaderRepo.flush();
    }
}
