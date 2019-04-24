package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.*;
import calc.entity.calc.inter.*;
import calc.formula.*;
import calc.formula.expression.impl.PowerLineExpression;
import calc.formula.expression.impl.PowerTransformerExpression;
import calc.formula.service.*;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import static calc.util.Util.*;
import static java.lang.Math.pow;
import static java.util.Arrays.*;
import static java.util.Optional.ofNullable;

@SuppressWarnings("ImplicitSubclassInspection")
@Service
@RequiredArgsConstructor
public class InterLineService {
    private static final Logger logger = LoggerFactory.getLogger(InterLineService.class);
    private static final String docCode = "INTER_LINE";
    private final InterResultLineRepo interResultLineRepo;
    private final InterResultNoteRepo interResultNoteRepo;
    private final InterResultAppRepo interResultAppRepo;
    private final MessageService messageService;
    private final ParamService paramService;
    private final CalcService calcService;
    private final PowerLineService powerLineService;

    public boolean calc(InterResultHeader header) {
        try {
            logger.info("started, headerId: " + header.getId());

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.INTER_MR)
                .build();

            messageService.deleteMessages(header);
            deleteLines(header);
            calcLines(header, context);
            copyNotes(header);
            copyApps(header);

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



    private void calcLines(InterResultHeader header, CalcContext context) {
        Parameter paramWL = paramService.getParam("WL");
        List<InterResultLine> results = new ArrayList<>();
        for (InterLine line : header.getHeader().getLines()) {
            Map<String, String> params = buildMsgParams(line);

            InterResultLine result = new InterResultLine();
            result.setHeader(header);
            result.setLineNum(line.getLineNum());
            result.setPowerLine(line.getPowerLine());
            result.setIsBoundMeterInst(line.getIsBoundMeterInst());
            result.setPowerLineLength(line.getPowerLineLength());
            result.setBoundMeteringPoint(line.getBoundMeteringPoint());
            result.setMeteringPointOut1(line.getMeteringPointOut1());
            result.setMeteringPointOut2(line.getMeteringPointOut2());
            result.setDefMethodValue1(line.getDefMethodValue1());
            result.setDefMethodValue2(line.getDefMethodValue2());
            result.setDefMethodLoss(line.getDefMethodLoss());
            result.setDefMethodLossShare(line.getDefMethodLossShare());
            result.setMpStatus1(line.getMpStatus1());
            result.setMpStatus2(line.getMpStatus2());
            result.setMeteringPoint1(line.getMeteringPoint1());
            result.setMeteringPoint2(line.getMeteringPoint2());
            result.setMethodOnBound(line.getMethodOnBound());
            result.setMvDefineBySide(line.getMvDefineBySide());
            result.setProportion1(line.getProportion1());
            result.setProportion2(line.getProportion2());
            result.setPowerLineLength1(line.getPowerLineLength1());
            result.setPowerLineLength2(line.getPowerLineLength2());
            result.setIsInverse1(line.getIsInverse1());
            result.setIsInverse2(line.getIsInverse2());
            result.setIsIncludeTotal(line.getIsIncludeTotal());
            result.setCreateDate(LocalDateTime.now());
            result.setCreateBy(header.getCreateBy());

            result.setDetails(ofNullable(result.getDetails())
                .orElse(new ArrayList<>()));


            for (Long direction : asList(1l, 2l)) {
                MeteringPoint meteringPoint1 = direction == 1l ? line.getMeteringPoint1() : line.getMeteringPoint2();
                MeteringPoint meteringPoint2 = direction == 1l ? line.getMeteringPoint2() : line.getMeteringPoint1();

                if (meteringPoint1 == null)
                    messageService.addMessage(header, line.getLineNum(), docCode, "INTER_SRC_MP_NOT_FOUND", params);

                if (meteringPoint2 == null)
                    messageService.addMessage(header, line.getLineNum(), docCode, "INTER_TRG_MP_NOT_FOUND", params);

                InterResultDetLine resultDet = new InterResultDetLine();
                resultDet.setHeader(header);
                resultDet.setLine(result);
                resultDet.setDirection(direction);
                resultDet.setMeteringPoint1(line.getMeteringPoint1());
                resultDet.setMeteringPoint2(line.getMeteringPoint2());
                resultDet.setCreateDate(LocalDateTime.now());
                resultDet.setCreateBy(header.getCreateBy());
                result.getDetails().add(resultDet);

                Parameter paramAm = paramService.getParam("A-");
                Parameter paramAp = paramService.getParam("A+");


                //Определение объёмов для точки учёта 1
                Double val1 = null;
                DefMethodValue defMethodValue1 = ofNullable(line.getDefMethodValue1()).orElse(DefMethodValue.DMV_MP_IS_NOT_USED);

                //по методу DMV_FACT_BY_MD
                if (defMethodValue1 == DefMethodValue.DMV_FACT_BY_MD) {
                    CalcResult calc = calcService.calcValue(meteringPoint1, paramAm, context);
                    val1 = calc != null ? calc.getDoubleValue() : null;
                    val1 = ofNullable(val1).orElse(0d);
                }

                //по методу DMV_FIXED_VALUE
                if (defMethodValue1 == DefMethodValue.DMV_FIXED_VALUE) {
                    CalcProperty property = CalcProperty.builder()
                        .contextType(ContextTypeEnum.DEFAULT)
                        .processOrder(ProcessOrderEnum.READ)
                        .build();

                    CalcResult calc = calcService.calcValue(meteringPoint1, paramAm, context, property);
                    val1 = calc != null ? calc.getDoubleValue() : null;
                    val1 = ofNullable(val1).orElse(0d);
                }

                //по методу DMV_OTHER_MP_AND_FULL_LOSSES
                if  (defMethodValue1 == DefMethodValue.DMV_OTHER_MP_AND_FULL_LOSSES) {

                }




                //Определение объёмов для точки учёта 2
                Double val2 = null;
                DefMethodValue defMethodValue2 = ofNullable(line.getDefMethodValue2()).orElse(DefMethodValue.DMV_MP_IS_NOT_USED);

                //по методу DMV_FACT_BY_MD (по приборам учета)
                if (defMethodValue2 == DefMethodValue.DMV_FACT_BY_MD) {
                    CalcResult calc = calcService.calcValue(meteringPoint2, paramAp, context);
                    val2 = calc != null ? calc.getDoubleValue() : null;
                    val2 = ofNullable(val2).orElse(0d);
                }


                //по методу DMV_FIXED_VALUE
                if (defMethodValue2 == DefMethodValue.DMV_FIXED_VALUE) {
                    CalcProperty property = CalcProperty.builder()
                        .contextType(ContextTypeEnum.DEFAULT)
                        .processOrder(ProcessOrderEnum.READ)
                        .build();

                    CalcResult calc = calcService.calcValue(meteringPoint2, paramAp, context, property);
                    val2 = calc != null ? calc.getDoubleValue() : null;
                    val2 = ofNullable(val2).orElse(0d);
                }

                //по методу DMV_OTHER_MP_AND_FULL_LOSSES
                if  (defMethodValue2 == DefMethodValue.DMV_OTHER_MP_AND_FULL_LOSSES) {

                }


                //Определение потерь
                Double lossVal = null;

                //По методу DML_BY_VOLUME_ON_BOTH_SIDES
                if (line.getDefMethodLoss() == DefMethodLoss.DML_BY_VOLUME_ON_BOTH_SIDES)
                    lossVal = val1 - val2;


                //По методу DML_AVG_FACTOR
                if (line.getDefMethodLoss() == DefMethodLoss.DML_AVG_FACTOR) {
                    logger.debug("calculation losses by method DML_AVG_FACTOR");

                    if (line.getMpStatus1() == DeviceStatus.MDS_IS_OK)
                        lossVal = ofNullable(val1).orElse(0d) * ofNullable(line.getAvgLossFactor()).orElse(0d);

                    if (line.getMpStatus2() == DeviceStatus.MDS_IS_OK)
                        lossVal = ofNullable(val2).orElse(0d) * ofNullable(line.getAvgLossFactor()).orElse(0d);
                }


                //По методу DML_WN_PLUS_WCD
                if (line.getDefMethodLoss() == DefMethodLoss.DML_WN_PLUS_WCD) {
                    logger.debug("calculation losses by method DML_WN_PLUS_WCD");

                    Double r = PowerLineExpression.builder()
                        .id(line.getPowerLine().getId())
                        .attr("r")
                        .context(context)
                        .service(powerLineService)
                        .build()
                        .doubleValue();
                    r = ofNullable(r).orElse(0d);


                    Double l = PowerLineExpression.builder()
                        .id(line.getPowerLine().getId())
                        .attr("length")
                        .context(context)
                        .service(powerLineService)
                        .build()
                        .doubleValue();
                    l = ofNullable(l).orElse(0d);


                    Double p = PowerLineExpression.builder()
                        .id(line.getPowerLine().getId())
                        .attr("po")
                        .context(context)
                        .service(powerLineService)
                        .build()
                        .doubleValue();
                    p = ofNullable(p).orElse(0d);


                    Double kf =  line.getLoadingNonlinearityFactor();
                    kf = ofNullable(kf).orElse(0d);

                    Double wp = direction == 1l ? val1 : val2;
                    wp = ofNullable(wp).orElse(0d);


                    Parameter paramU = paramService.getParam("U");
                    Parameter paramTp = paramService.getParam("T+");
                    Parameter paramTm = paramService.getParam("T-");
                    MeteringPoint mp = null;

                    Long mvDefineBySide = line.getMvDefineBySide();

                    if (mvDefineBySide != null)
                         mp = mvDefineBySide.equals(1l) ? line.getMeteringPoint1() : line.getMeteringPoint2();

                    CalcProperty property = CalcProperty.builder()
                        .contextType(ContextTypeEnum.DEFAULT)
                        .processOrder(ProcessOrderEnum.READ)
                        .build();

                    Double u = null;
                    Double tp = null;
                    Double tm = null;
                    if (mp != null) {
                        if (paramU != null) {
                            CalcResult calc = calcService.calcValue(mp, paramU, context, property);
                            u = calc != null ? calc.getDoubleValue() : null;
                        }

                        if (paramTp !=null) {
                            CalcResult calc = calcService.calcValue(mp, paramTp, context, property);
                            tp = calc != null ? calc.getDoubleValue() : null;

                        }

                        if (paramTm != null) {
                            CalcResult calc = calcService.calcValue(mp, paramTm, context, property);
                            tm = calc != null ? calc.getDoubleValue() : null;
                        }
                    }

                    u = ofNullable(u).orElse(0d);
                    tp = ofNullable(tp).orElse(0d);
                    tm = ofNullable(tm).orElse(0d);
                    Double t = direction == 1l ? tp : tm;

                    Double wn = null;
                    if (u != 0d && t !=0d)
                        wn = pow(wp,2) / (pow(u,2) * t) * r * pow(kf,2);
                    wn = ofNullable(wn).orElse(0d);

                    Double wk = p * t * l;
                    wk = ofNullable(wk).orElse(0d);

                    lossVal = wn + wk;

                    logger.debug("  direction: " + direction);
                    logger.debug("  powerLineId: " + line.getPowerLine().getId());
                    logger.debug("  r: " + r);
                    logger.debug("  l: " + l);
                    logger.debug("  p: " + p);
                    logger.debug("  kf: " + kf);
                    logger.debug("  wp: " + wp);
                    logger.debug("  mvDefineBySide: " + mvDefineBySide);
                    logger.debug("  mp: " + mp.getId());
                    logger.debug("  u: " + u);
                    logger.debug("  tp: " + tp);
                    logger.debug("  tm: " + tm);
                    logger.debug("  t: " + t);
                    logger.debug("  wn: " + wn);
                    logger.debug("  wk: " + wk);
                    logger.debug("  lossVal: " + lossVal);
                }



                //Определение долей потерь

                Double lossProc1 = null;
                Double lossProc2 = null;
                if (line.getDefMethodLossShare() == DefMethodLossShare.DMLS_PROCENT_SHARE) {
                    lossProc1 = ofNullable(line.getProportion1()).orElse(0d);
                    lossProc2 = ofNullable(line.getProportion2()).orElse(0d);
                }

                if (line.getDefMethodLossShare() == DefMethodLossShare.DMLS_LENGTH_SHARE) {
                    Double powerLength = ofNullable(line.getPowerLineLength()).orElse(0d);
                    Double powerLength1 = ofNullable(line.getPowerLineLength1()).orElse(0d);
                    lossProc1 = round(100d * powerLength1 / powerLength, 6);
                    lossProc2 = 100d - lossProc1;
                }

                if (line.getDefMethodLossShare() == DefMethodLossShare.DMLS_FIX_VALUE_ON_SIDE1) {

                }



                //Определение перетока на границе раздела
                CalcMethodBound methodOnBound = line.getMethodOnBound();
                Double lossVal1 = null;
                Double lossVal2 = null;
                Double boundVal = null;

                //по методу DMBP_SIDE1_AND_LOSS_SHARE
                if (methodOnBound == CalcMethodBound.DMBP_SIDE1_AND_LOSS_SHARE) {
                    lossVal1 = round(ofNullable(lossProc1).orElse(0d) / 100d * ofNullable(lossVal).orElse(0d), paramWL);
                    lossVal2 = ofNullable(lossVal).orElse(0d) - lossVal1;
                    boundVal = ofNullable(val1).orElse(0d) - (direction == 1l ? lossVal1 : lossVal2);
                }

                //по методу DMBP_SIDE2_AND_LOSS_SHARE
                if (methodOnBound == CalcMethodBound.DMBP_SIDE2_AND_LOSS_SHARE) {
                    lossVal2 = round(ofNullable(lossProc2).orElse(0d) / 100d * ofNullable(lossVal).orElse(0d), paramWL);
                    lossVal1 = lossVal - lossVal2;
                    boundVal = val2 + (direction == 1l ? lossVal2 : lossVal1);
                }

                //по методу DMBP_MP_ON_BORDER
                if (methodOnBound == CalcMethodBound.DMBP_MP_ON_BORDER) {
                    CalcResult calc = calcService.calcValue(line.getBoundMeteringPoint(), paramAm, context);
                    Double valAm = calc != null ? calc.getDoubleValue() : null;

                    calc = calcService.calcValue(line.getBoundMeteringPoint(), paramAp, context);
                    Double valAp = calc != null ? calc.getDoubleValue() : null;

                    boundVal = direction == 1L ? valAm : valAp;

                    if (boundVal != null)
                        boundVal = line.getIsInverse() ? boundVal * -1d : boundVal;
                }

                resultDet.setVal1(val1);
                resultDet.setVal2(val2);
                resultDet.setLossVal(lossVal);
                resultDet.setLossProc1(lossProc1);
                resultDet.setLossProc2(lossProc2);
                resultDet.setLossVal1(lossVal1);
                resultDet.setLossVal2(lossVal2);
                resultDet.setBoundVal(boundVal);
            }

            Double boundaryVal = result.getDetails()
                .stream()
                .map(t -> t.getDirection() == 1l ? -1 * ofNullable(t.getBoundVal()).orElse(0d) : ofNullable(t.getBoundVal()).orElse(0d))
                .reduce((t1, t2) -> t1 + t2)
                .orElse(0d);

            result.setBoundaryVal(boundaryVal);
            results.add(result);
        }
        saveLines(results);
    }

    /*
    private void calcLines(InterResultHeader header, CalcContext context) {
        Parameter paramWL = paramService.getParam("WL");
        List<InterResultLine> results = new ArrayList<>();
        for (InterLine line : header.getHeader().getLines()) {
            Map<String, String> params = buildMsgParams(line);

            InterResultLine result = new InterResultLine();
            result.setHeader(header);
            result.setLineNum(line.getLineNum());
            result.setPowerLine(line.getPowerLine());
            result.setIsBoundMeterInst(line.getIsBoundMeterInst());
            result.setPowerLineLength(line.getPowerLineLength());
            result.setBoundMeteringPoint(line.getBoundMeteringPoint());
            result.setMeteringPointOut1(line.getMeteringPointOut1());
            result.setMeteringPointOut2(line.getMeteringPointOut2());
            result.setIsIncludeTotal(line.getIsIncludeTotal());
            result.setCreateDate(LocalDateTime.now());
            result.setCreateBy(header.getCreateBy());

            result.setDetails(ofNullable(result.getDetails())
                .orElse(new ArrayList<>()));

            if (line.getIsBoundMeterInst()) {
                InterResultDetLine resultDet = new InterResultDetLine();
                resultDet.setHeader(header);
                resultDet.setLine(result);
                resultDet.setDirection(1l);
                resultDet.setMeteringPoint1(line.getMeteringPoint1());
                resultDet.setMeteringPoint2(line.getMeteringPoint2());
                resultDet.setCreateDate(LocalDateTime.now());
                resultDet.setCreateBy(header.getCreateBy());
                result.getDetails().add(resultDet);

                if (line.getBoundMeteringPoint() == null)
                    messageService.addMessage(header, line.getLineNum(), docCode, "INTER_BOUND_MP_NOT_FOUND", params);

                Parameter paramAp = paramService.getParam("A+");
                Parameter paramAm = paramService.getParam("A-");

                if (line.getBoundMeteringPoint() != null) {
                    CalcResult calc = calcService.calcValue(line.getBoundMeteringPoint(), paramAm, context);
                    Double val1 = calc != null ? calc.getDoubleValue() : null;

                    calc = calcService.calcValue(line.getBoundMeteringPoint(), paramAp, context);
                    Double val2 = calc != null ? calc.getDoubleValue() : null;

                    Double val = null;
                    if (val1 != null || val2 != null)
                        val = ofNullable(val1).orElse(0d) - ofNullable(val2).orElse(0d);

                    if (val != null)
                        result.setBoundaryVal(line.getIsInverse() ?  val * -1d : val);

                    resultDet.setVal1(val1);
                    resultDet.setVal2(val2);

                    if (val != null)
                        resultDet.setBoundaryVal(Math.abs(val));
                }
            }
            else  {
                for (Long direction : asList(1l, 2l)) {
                    MeteringPoint meteringPoint1 = direction == 1l ? line.getMeteringPoint1() : line.getMeteringPoint2();
                    MeteringPoint meteringPoint2 = direction == 1l ? line.getMeteringPoint2() : line.getMeteringPoint1();

                    if (meteringPoint1 == null)
                        messageService.addMessage(header, line.getLineNum(), docCode, "INTER_SRC_MP_NOT_FOUND", params);

                    if (meteringPoint2 == null)
                        messageService.addMessage(header, line.getLineNum(), docCode, "INTER_TRG_MP_NOT_FOUND", params);

                    Parameter paramAp = paramService.getParam("A+");
                    Parameter paramAm = paramService.getParam("A-");

                    InterResultDetLine resultDet = new InterResultDetLine();
                    resultDet.setHeader(header);
                    resultDet.setLine(result);
                    resultDet.setDirection(direction);
                    resultDet.setMeteringPoint1(line.getMeteringPoint1());
                    resultDet.setMeteringPoint2(line.getMeteringPoint2());
                    resultDet.setCreateDate(LocalDateTime.now());
                    resultDet.setCreateBy(header.getCreateBy());

                    CalcResult calc = calcService.calcValue(meteringPoint1, paramAm, context);
                    Double val1 = calc != null ? calc.getDoubleValue() : null;

                    calc = calcService.calcValue(meteringPoint2, paramAp, context);
                    Double val2 = calc != null ? calc.getDoubleValue() : null;

                    val1 = ofNullable(val1).orElse(0d);
                    val2 = ofNullable(val2).orElse(0d);
                    Double lossVal = val1 - val2;

                    Double lossProc1 = ofNullable(line.getProportion1()).orElse(0d);
                    Double lossProc2 = ofNullable(line.getProportion2()).orElse(0d);

                    Double powerLength = ofNullable(line.getPowerLineLength()).orElse(0d);
                    Double powerLength1 = ofNullable(line.getPowerLineLength1()).orElse(0d);

                    if (line.getIsProportionLength() && powerLength != 0d) {
                        lossProc1 = round(100d * powerLength1 / powerLength, 4);
                        lossProc2 = 100d - lossProc1;
                    }

                    Double lossVal1 = round(lossProc1 / 100d * lossVal,paramWL);
                    Double lossVal2 = lossVal - lossVal1;
                    Double boundaryVal = val1 - (direction == 1l ? lossVal1 : lossVal2);

                    resultDet.setVal1(val1);
                    resultDet.setVal2(val2);
                    resultDet.setLossVal(lossVal);
                    resultDet.setLossProc1(lossProc1);
                    resultDet.setLossProc2(lossProc2);
                    resultDet.setLossVal1(lossVal1);
                    resultDet.setLossVal2(lossVal2);
                    resultDet.setBoundaryVal(boundaryVal);
                    result.getDetails().add(resultDet);
                }

                Double totalBoundaryVal = result.getDetails()
                    .stream()
                    .map(t -> t.getDirection() == 1l ? -1 * t.getBoundaryVal() : t.getBoundaryVal())
                    .reduce((t1, t2) -> t1 + t2)
                    .orElse(0d);

                result.setBoundaryVal(totalBoundaryVal);
            }
            results.add(result);
        }
        saveLines(results);
    }
    */

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void saveLines(List<InterResultLine> resultLines) {
        interResultLineRepo.save(resultLines);
        interResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void deleteLines(InterResultHeader header) {
        List<InterResultLine> lines = interResultLineRepo.findAllByHeaderId(header.getId());
        interResultLineRepo.delete(lines);
        interResultLineRepo.flush();

        List<InterResultNote> notes = interResultNoteRepo.findAllByHeaderId(header.getId());
        interResultNoteRepo.delete(notes);
        interResultNoteRepo.flush();

        List<InterResultApp> apps = interResultAppRepo.findAllByHeaderId(header.getId());
        interResultAppRepo.delete(apps);
        interResultAppRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void copyNotes(InterResultHeader header) {
        List<InterResultNote> resultNotes = new ArrayList<>();
        for (InterNote note : header.getHeader().getNotes()) {
            InterResultNote resultNote = new InterResultNote();
            resultNote.setHeader(header);
            resultNote.setNoteNum(note.getNoteNum());
            resultNote.setLineNum(note.getLineNum());

            resultNote.setTranslates(ofNullable(resultNote.getTranslates()).orElse(new ArrayList<>()));
            for (InterNoteTranslate noteTranslate : note.getTranslates()) {
                InterResultNoteTranslate resultNoteTranslate = new InterResultNoteTranslate();
                resultNoteTranslate.setNote(resultNote);
                resultNoteTranslate.setLang(noteTranslate.getLang());
                resultNoteTranslate.setNoteText(noteTranslate.getNoteText());
                resultNote.getTranslates().add(resultNoteTranslate);
            }
            resultNotes.add(resultNote);
        }
        interResultNoteRepo.save(resultNotes);
        interResultNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void copyApps(InterResultHeader header) {
        List<InterResultApp> resultApps = new ArrayList<>();
        for (InterApp app : header.getHeader().getApps()) {
            InterResultApp resultApp = new InterResultApp();
            resultApp.setHeader(header);
            resultApp.setAppNum(app.getAppNum());

            resultApp.setTranslates(ofNullable(resultApp.getTranslates()).orElse(new ArrayList<>()));
            for (InterAppTranslate appTranslate : app.getTranslates()) {
                InterResultAppTranslate resultAppTranslate = new InterResultAppTranslate();
                resultAppTranslate.setApp(resultApp);
                resultAppTranslate.setLang(appTranslate.getLang());
                resultAppTranslate.setAppName(appTranslate.getAppName());
                resultApp.getTranslates().add(resultAppTranslate);
            }
            resultApps.add(resultApp);
        }
        interResultAppRepo.save(resultApps);
        interResultAppRepo.flush();
    }
}
