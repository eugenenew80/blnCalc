package calc.formula.calculation;

import calc.entity.calc.MeteringPoint;
import calc.entity.calc.inter.*;
import calc.formula.CalcContext;
import calc.formula.ContextType;
import calc.formula.expression.impl.BinaryExpression;
import calc.formula.expression.impl.InterMrExpression;
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
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class InterLineService {
    private static final Logger logger = LoggerFactory.getLogger(InterLineService.class);
    private static final String docCode = "INTER_LINE";
    private final InterResultLineRepo interResultLineRepo;
    private final InterResultMrService interMrService;
    private final InterResultNoteRepo interResultNoteRepo;
    private final InterResultAppRepo interResultAppRepo;
    private final OperatorFactory operatorFactory;

    public boolean calc(InterResultHeader header) {
        try {
            logger.info("Lines for inter with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .contextType(ContextType.INTER)
                .values(new HashMap<>())
                .build();

            List<InterResultLine> resultLines = new ArrayList<>();
            for (InterLine line : header.getHeader().getLines()) {
                InterResultLine resultLine = new InterResultLine();
                resultLine.setHeader(header);
                resultLine.setLineNum(line.getLineNum());
                resultLine.setPowerLine(line.getPowerLine());
                resultLine.setIsBoundMeterInst(line.getIsBoundMeterInst());
                resultLine.setPowerLineLength(line.getPowerLineLength());
                resultLine.setBoundMeteringPoint(line.getBoundMeteringPoint());
                resultLine.setCreateDate(LocalDateTime.now());
                resultLine.setCreateBy(header.getCreateBy());
                resultLine.setDetails(ofNullable(resultLine.getDetails()).orElse(new ArrayList<>()));

                if (line.getIsBoundMeterInst()) {
                    InterResultDetLine resultDetLine = new InterResultDetLine();
                    resultDetLine.setHeader(header);
                    resultDetLine.setLine(resultLine);
                    resultDetLine.setDirection(1l);
                    resultDetLine.setLossProc1(null);
                    resultDetLine.setLossProc2(null);
                    resultDetLine.setLossVal(null);
                    resultDetLine.setLossVal1(null);
                    resultDetLine.setLossVal2(null);
                    resultDetLine.setMeteringPoint1(line.getMeteringPoint1());
                    resultDetLine.setMeteringPoint2(line.getMeteringPoint2());
                    resultDetLine.setVal1(null);
                    resultDetLine.setVal2(null);
                    resultDetLine.setCreateDate(LocalDateTime.now());
                    resultDetLine.setCreateBy(header.getCreateBy());
                    resultLine.getDetails().add(resultDetLine);

                    InterMrExpression expression1 = InterMrExpression.builder()
                        .context(context)
                        .meteringPointCode(line.getBoundMeteringPoint().getCode())
                        .parameterCode("A+")
                        .rate(1d)
                        .service(interMrService)
                        .build();

                    InterMrExpression expression2 = InterMrExpression.builder()
                        .context(context)
                        .meteringPointCode(line.getBoundMeteringPoint().getCode())
                        .parameterCode("A-")
                        .rate(1d)
                        .service(interMrService)
                        .build();

                    Double val = BinaryExpression.builder()
                        .operator(operatorFactory.binary("min"))
                        .expressions(Arrays.asList(expression1, expression2))
                        .build()
                        .doubleValue();

                    resultLine.setBoundaryVal(val);
                }

                if (!line.getIsBoundMeterInst()) {
                    for (Long direction : Arrays.asList(1l, 2l)) {
                        MeteringPoint meteringPoint1 = direction == 1l ? line.getMeteringPoint1() : line.getMeteringPoint2();
                        MeteringPoint meteringPoint2 = direction == 1l ? line.getMeteringPoint2() : line.getMeteringPoint1();

                        InterResultDetLine resultDetLine = new InterResultDetLine();
                        resultDetLine.setHeader(header);
                        resultDetLine.setLine(resultLine);
                        resultDetLine.setDirection(direction);
                        resultDetLine.setMeteringPoint1(meteringPoint1);
                        resultDetLine.setMeteringPoint2(meteringPoint2);
                        resultDetLine.setCreateDate(LocalDateTime.now());
                        resultDetLine.setCreateBy(header.getCreateBy());

                        Double val1 = null;
                        if (meteringPoint1 != null ) {
                            val1 = InterMrExpression.builder()
                                .context(context)
                                .meteringPointCode(meteringPoint1.getCode())
                                .parameterCode("A-")
                                .rate(1d)
                                .service(interMrService)
                                .build()
                                .doubleValue();
                        }

                        Double val2 = null;
                        if (meteringPoint2 != null ) {
                            val2 = InterMrExpression.builder()
                                .context(context)
                                .meteringPointCode(meteringPoint2.getCode())
                                .parameterCode("A+")
                                .rate(1d)
                                .service(interMrService)
                                .build()
                                .doubleValue();
                        }

                        val1 = ofNullable(val1).orElse(0d);
                        val2 = ofNullable(val2).orElse(0d);
                        Double lossVal = val1 - val2;

                        Double lossProc1 = ofNullable(line.getProportion1()).orElse(0d);
                        Double lossProc2 = ofNullable(line.getProportion2()).orElse(0d);

                        Double powerLength = ofNullable(line.getPowerLineLength()).orElse(0d);
                        Double powerLength1 = ofNullable(line.getPowerLineLength1()).orElse(0d);
                        Double powerLength2 = ofNullable(line.getPowerLineLength2()).orElse(0d);
                        if (line.getIsProportionLength() && powerLength != 0d) {
                            lossProc1 = 100d * powerLength1 / powerLength;
                            lossProc2 = 100d * powerLength2 / powerLength;
                        }

                        Double lossVal1 = lossProc1 / 100d * lossVal;
                        Double lossVal2 = lossProc2 / 100d * lossVal;
                        Double boundaryVal = val1 - lossVal1;

                        resultDetLine.setVal1(val1);
                        resultDetLine.setVal2(val2);
                        resultDetLine.setLossVal(lossVal);
                        resultDetLine.setLossProc1(lossProc1);
                        resultDetLine.setLossProc2(lossProc2);
                        resultDetLine.setLossVal1(lossVal1);
                        resultDetLine.setLossVal2(lossVal2);
                        resultDetLine.setBoundaryVal(boundaryVal);
                        resultLine.getDetails().add(resultDetLine);
                    }

                    Double totalBoundaryVal = resultLine.getDetails()
                        .stream()
                        .map(t -> t.getDirection() == 1l ? -1 * t.getBoundaryVal() : t.getBoundaryVal())
                        .reduce((t1, t2) -> t1 + t2)
                        .orElse(0d);

                    resultLine.setBoundaryVal(totalBoundaryVal);
                }
                resultLines.add(resultLine);
            }

            deleteLines(header);
            saveLines(resultLines);
            copyNotes(header);
            copyApps(header);
            return true;
        }

        catch (Exception e) {
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<InterResultLine> resultLines) {
        interResultLineRepo.save(resultLines);
        interResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(InterResultHeader header) {
        List<InterResultLine> lines = interResultLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            interResultLineRepo.delete(lines.get(i));
        interResultLineRepo.flush();

        List<InterResultNote> notes = interResultNoteRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<notes.size(); i++)
            interResultNoteRepo.delete(notes.get(i));
        interResultNoteRepo.flush();

        List<InterResultApp> apps = interResultAppRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<apps.size(); i++)
            interResultAppRepo.delete(apps.get(i));
        interResultAppRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void copyNotes(InterResultHeader header) {
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
    void copyApps(InterResultHeader header) {
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