package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.*;
import calc.formula.CalcContext;
import calc.formula.expression.impl.MrExpression;
import calc.formula.service.BalanceSubstResultMrService;
import calc.formula.service.MessageService;
import calc.formula.service.ParamService;
import calc.repo.calc.BalanceSubstResultHeaderRepo;
import calc.repo.calc.BalanceSubstResultLineRepo;
import calc.repo.calc.BalanceSubstResultNoteRepo;
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
public class BalanceSubstLineService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstLineService.class);
    private final BalanceSubstResultHeaderRepo balanceSubstResultHeaderRepo;
    private final BalanceSubstResultLineRepo balanceSubstResultLineRepo;
    private final ParamService paramService;
    private final BalanceSubstResultMrService mrService;
    private final BalanceSubstResultNoteRepo balanceSubstResultNoteRepo;
    private final MessageService messageService;
    private static final String docCode = "BALANCE_LINES";
    private Map<String, Parameter> mapParams = null;

    @PostConstruct
    public void init() {
        mapParams = paramService.getValues();
    }

    public boolean calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Lines for balance substation with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .docCode(docCode)
                .docId(header.getId())
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .isMeteringReading(true)
                .trace(new HashMap<>())
                .values(new HashMap<>())
                .build();

            List<BalanceSubstResultLine> resultLines = new ArrayList<>();
            for (BalanceSubstLine line : header.getHeader().getLines()) {
                MeteringPoint meteringPoint = line.getMeteringPoint();
                if (meteringPoint == null) continue;

                Map<String, String> sections = getSections(line);
                for (String section : sections.keySet()) {
                    String param = line.getParam() == null ? sections.get(section) : line.getParam().getCode();
                    param = inverseParam(param, line.getIsInverse());
                    Double val = getMrVal(line, param, context);

                    BalanceSubstResultLine resultLine = new BalanceSubstResultLine();
                    resultLine.setHeader(header);
                    resultLine.setMeteringPoint(meteringPoint);
                    resultLine.setParam(mapParams.get(param));
                    resultLine.setRate(Optional.ofNullable(line.getRate()).orElse(1d));
                    resultLine.setSection(section);
                    resultLine.setVal(val);
                    if (meteringPoint.getVoltageClass()!=null)
                        resultLine.setSubSection(meteringPoint.getVoltageClass().getValue().toString());

                    addTranslates(line, resultLine);
                    resultLines.add(resultLine);
                }
            }

            deleteLines(header);
            saveLines(resultLines);
            copyNotes(header);
            return true;
        }

        catch (Exception e) {
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION");
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void saveLines(List<BalanceSubstResultLine> resultLines) {
        balanceSubstResultLineRepo.save(resultLines);
        balanceSubstResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultLine> lines = balanceSubstResultLineRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<lines.size(); i++)
            balanceSubstResultLineRepo.delete(lines.get(i));
        balanceSubstResultLineRepo.flush();

        List<BalanceSubstResultNote> notes = balanceSubstResultNoteRepo.findAllByHeaderId(header.getId());
        for (int i=0; i<notes.size(); i++)
            balanceSubstResultNoteRepo.delete(notes.get(i));
        balanceSubstResultNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void copyNotes(BalanceSubstResultHeader header) {
        List<BalanceSubstResultNote> resultNotes = new ArrayList<>();
        for (BalanceSubstNote note : header.getHeader().getNotes()) {
            BalanceSubstResultNote resultNote = new BalanceSubstResultNote();
            resultNote.setHeader(header);
            resultNote.setMeteringPoint(note.getMeteringPoint());
            resultNote.setLineNum(note.getLineNum());

            if (resultNote.getTranslates() == null)
                resultNote.setTranslates(new ArrayList<>());

            for (BalanceSubstNoteTranslate noteTranslate : note.getTranslates()) {
                BalanceSubstResultNoteTranslate resultNoteTranslate = new BalanceSubstResultNoteTranslate();
                resultNoteTranslate.setNote(resultNote);
                resultNoteTranslate.setLang(noteTranslate.getLang());
                resultNoteTranslate.setNoteText(noteTranslate.getNoteText());
                resultNote.getTranslates().add(resultNoteTranslate);
            }
            resultNotes.add(resultNote);
        }
        balanceSubstResultNoteRepo.save(resultNotes);
        balanceSubstResultNoteRepo.flush();
    }


    private void addTranslates(BalanceSubstLine line, BalanceSubstResultLine resultLine) {
        if (resultLine.getTranslates() == null)
            resultLine.setTranslates(new ArrayList<>());

        for (BalanceSubstLineTranslate lineTranslate : line.getTranslates()) {
            BalanceSubstResultLineTranslate resultLineTranslate = new BalanceSubstResultLineTranslate();
            resultLineTranslate.setLang(lineTranslate.getLang());
            resultLineTranslate.setLine(resultLine);
            resultLineTranslate.setName(lineTranslate.getName());
            resultLine.getTranslates().add(resultLineTranslate);
        }
    }

    private Double getMrVal(BalanceSubstLine bsLine, String param, CalcContext context) {
        return MrExpression.builder()
            .meteringPointCode(bsLine.getMeteringPoint().getCode())
            .parameterCode(param)
            .rate(bsLine.getRate())
            .context(context)
            .service(mrService)
            .build()
            .doubleValue();
    }

    private Map<String, String> getSections(BalanceSubstLine bLine) {
        Map<String, String> map = new HashMap<>();
        if (bLine.getIsSection1()) map.put("1", "A+");
        if (bLine.getIsSection2()) map.put("2", "A-");
        if (bLine.getIsSection3()) map.put("3", "A-");
        if (bLine.getIsSection4()) map.put("4", "A-");
        return map;
    }

    private String inverseParam(String param, Boolean isInverse) {
        if (isInverse) {
            if (param.equals("A+")) return "A-";
            if (param.equals("A-")) return "A+";
            if (param.equals("R+")) return "R-";
            if (param.equals("R-")) return "R+";
        }
        return param;
    }
}
