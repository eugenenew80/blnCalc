package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.*;
import calc.entity.calc.bs.mr.*;
import calc.formula.CalcContext;
import calc.formula.ContextType;
import calc.formula.service.MessageService;
import calc.formula.service.MeteringReading;
import calc.formula.service.MrService;
import calc.formula.service.ParamService;
import calc.repo.calc.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceSubstMrService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstMrService.class);
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final MrService mrService;
    private final ParamService paramService;
    private final BalanceSubstResultMrNoteRepo balanceSubstResultMrNoteRepo;
    private final MessageService messageService;
    private static final String docCode = "ACT";
    private Map<String, Parameter> mapParams = null;

    @PostConstruct
    public void init() {
        mapParams = paramService.getValues();
    }

    public boolean calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Act for balance with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .contextType(ContextType.DEFAULT)
                .values(new HashMap<>())
                .build();

            List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
            for (BalanceSubstMrLine mrLine : header.getHeader().getMrLines()) {
                MeteringPoint meteringPoint = mrLine.getMeteringPoint();
                if (meteringPoint == null)
                    continue;

                String info = meteringPoint.getCode();

                List<MeteringReading> meteringReadings = mrService.calc(meteringPoint, context);
                for (MeteringReading t : meteringReadings) {
                    BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();

                    if (t.getMeter() == null)
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_METER_NOT_FOUND", info);

                    if (t.getMeterHistory() == null)
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_METER_HISTORY_NOT_FOUND", info);

                    String section = getSection(mrLine);
                    if (section == null || section.equals("")) {
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_SECTION_NOT_FOUND", info);
                        continue;
                    }

                    line.setHeader(header);
                    line.setMeteringPoint(meteringPoint);
                    line.setSection(section);
                    line.setBypassMeteringPoint(t.getBypassMeteringPoint());
                    line.setBypassMode(t.getBypassMode());
                    line.setIsBypassSection(t.getIsBypassSection());
                    line.setIsIgnore(false);
                    line.setParam(inverseParam(t.getParam(), mrLine.getIsInverse()));
                    line.setUnit(t.getUnit());
                    line.setMeter(t.getMeter());
                    line.setMeterHistory(t.getMeterHistory());
                    line.setStartMeteringDate(t.getStartMeteringDate());
                    line.setEndMeteringDate(t.getEndMeteringDate());
                    line.setStartVal(t.getStartVal());
                    line.setEndVal(t.getEndVal());
                    line.setDelta(t.getDelta());
                    line.setMeterRate(t.getMeterRate());
                    line.setVal(t.getVal());
                    line.setUnderCountVal(t.getUnderCountVal());
                    line.setUndercount(t.getUnderCount());
                    if (meteringPoint.getVoltageClass()!=null)
                        line.setSubSection(meteringPoint.getVoltageClass().getValue().toString());

                    resultLines.add(line);
                }
            }

            deleteLines(header);
            saveLines(resultLines);
            copyNotes(header);

            logger.info("Act for balance with headerId " + header.getId() + " completed");
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", e.getClass().getCanonicalName());
            logger.error("Act for balance with headerId " + header.getId() + " terminated with exception: " + e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<BalanceSubstResultMrLine> resultLines) {
        balanceSubstResultMrLineRepo.save(resultLines);
        balanceSubstResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMrLine> lines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultMrLineRepo.delete(lines.get(i));
        balanceSubstResultMrLineRepo.flush();

        List<BalanceSubstResultMrNote> notes = balanceSubstResultMrNoteRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<notes.size(); i++)
            balanceSubstResultMrNoteRepo.delete(notes.get(i));
        balanceSubstResultMrNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void copyNotes(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMrNote> resultNotes = new ArrayList<>();
        for (BalanceSubstMrNote note : header.getHeader().getMrNotes()) {
            BalanceSubstResultMrNote resultNote = new BalanceSubstResultMrNote();
            resultNote.setHeader(header);
            resultNote.setMeteringPoint(note.getMeteringPoint());
            resultNote.setLineNum(note.getLineNum());

            resultNote.setTranslates(Optional.ofNullable(resultNote.getTranslates()).orElse(new ArrayList<>()));
            for (BalanceSubstMrNoteTranslate noteTranslate : note.getTranslates()) {
                BalanceSubstResultMrNoteTranslate resultNoteTranslate = new BalanceSubstResultMrNoteTranslate();
                resultNoteTranslate.setNote(resultNote);
                resultNoteTranslate.setLang(noteTranslate.getLang());
                resultNoteTranslate.setNoteText(noteTranslate.getNoteText());
                resultNote.getTranslates().add(resultNoteTranslate);
            }
            resultNotes.add(resultNote);
        }
        balanceSubstResultMrNoteRepo.save(resultNotes);
        balanceSubstResultMrNoteRepo.flush();
    }

    private String getSection(BalanceSubstMrLine mrLine) {
        if (mrLine.getIsSection1()) return "1";
        if (mrLine.getIsSection2()) return "2";
        if (mrLine.getIsSection3()) return "3";
        if (mrLine.getIsSection4()) return "4";
        if (mrLine.getIsSection5()) return "5";
        return "";
    }

    private Parameter inverseParam(Parameter param, Boolean isInverse) {
        if (isInverse) {
            if (param.getCode().equals("A+")) return mapParams.get("A-");
            if (param.getCode().equals("A-")) return mapParams.get("A+");
            if (param.getCode().equals("R+")) return mapParams.get("R-");
            if (param.getCode().equals("R-")) return mapParams.get("R+");
        }
        return param;
    }
}
