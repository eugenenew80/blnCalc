package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.enums.LangEnum;
import calc.entity.calc.inter.*;
import calc.formula.*;
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

                Parameter paramAp = inverseParam(paramService, paramService.getParam("A+"), line.getIsInverse());
                Parameter paramAm = inverseParam(paramService, paramService.getParam("A-"), line.getIsInverse());

                if (line.getBoundMeteringPoint() != null) {
                    CalcResult calc = calcService.calcValue(line.getBoundMeteringPoint(), paramAm, context);
                    Double val1 = calc != null ? calc.getDoubleValue() : null;

                    calc = calcService.calcValue(line.getBoundMeteringPoint(), paramAp, context);
                    Double val2 = calc != null ? calc.getDoubleValue() : null;

                    Double val = null;
                    if (val1 != null || val2 != null)
                        val = ofNullable(val2).orElse(0d) - ofNullable(val).orElse(0d);

                    result.setBoundaryVal(val);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveLines(List<InterResultLine> resultLines) {
        interResultLineRepo.save(resultLines);
        interResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
