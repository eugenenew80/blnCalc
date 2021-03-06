package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.*;
import calc.entity.calc.enums.LangEnum;
import calc.formula.*;
import calc.formula.exception.CalcServiceException;
import calc.formula.service.*;
import calc.repo.calc.BalanceSubstResultLineRepo;
import calc.repo.calc.BalanceSubstResultNoteRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static calc.util.Util.buildMsgParams;
import static calc.util.Util.inverseParam;
import static java.util.Optional.*;

@SuppressWarnings("ImplicitSubclassInspection")
@Service
@RequiredArgsConstructor
public class BalanceSubstLineService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstLineService.class);
    private static final String docCode = "BALANCE";
    private final BalanceSubstResultLineRepo balanceSubstResultLineRepo;
    private final BalanceSubstResultNoteRepo balanceSubstResultNoteRepo;
    private final CalcService calcService;
    private final MessageService messageService;
    private final ParamService paramService;

    public boolean calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Lines for balance substation with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.MR)
                .build();

            deleteLines(header);
            calcLines(header, context);
            copyNotes(header);
            return true;
        }

        catch (Exception e) {
            logger.error(e.toString() + ": " + e.getMessage());
            e.printStackTrace();

            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", buildMsgParams(e));
            return false;
        }
    }

    private void calcLines(BalanceSubstResultHeader header, CalcContext context) {
        List<BalanceSubstResultLine> resultLines = new ArrayList<>();
        for (BalanceSubstLine line : header.getHeader().getLines()) {
            MeteringPoint meteringPoint = line.getMeteringPoint();
            if (meteringPoint == null) continue;

            Map<String, Parameter> sections = getSections(line);
            for (String section : sections.keySet()) {
                Parameter param = line.getParam() == null ? sections.get(section) : line.getParam();
                param = inverseParam(paramService, param, line.getIsInverse());

                Map<String, String> msgParams = buildMsgParams(meteringPoint);
                Double val = null;
                try {
                    val = getMrVal(line, param, context);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, line.getId(), docCode, e.getErrCode(), msgParams);
                }

                BalanceSubstResultLine resultLine = new BalanceSubstResultLine();
                resultLine.setHeader(header);
                resultLine.setMeteringPoint(meteringPoint);
                resultLine.setParam(param);
                resultLine.setRate(ofNullable(line.getRate()).orElse(1d));
                resultLine.setSection(section);
                resultLine.setVal(val);
                if (meteringPoint.getVoltageClass() != null)
                    resultLine.setSubSection(meteringPoint.getVoltageClass().getValue().toString());

                addTranslates(line, resultLine);
                resultLines.add(resultLine);
            }
        }
        saveLines(resultLines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void saveLines(List<BalanceSubstResultLine> resultLines) {
        balanceSubstResultLineRepo.save(resultLines);
        balanceSubstResultLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultLine> lines = balanceSubstResultLineRepo.findAllByHeaderId(header.getId());
        balanceSubstResultLineRepo.delete(lines);
        balanceSubstResultLineRepo.flush();

        List<BalanceSubstResultNote> notes = balanceSubstResultNoteRepo.findAllByHeaderId(header.getId());
        balanceSubstResultNoteRepo.delete(notes);
        balanceSubstResultNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    private void copyNotes(BalanceSubstResultHeader header) {
        List<BalanceSubstResultNote> results = new ArrayList<>();
        for (BalanceSubstNote note : header.getHeader().getNotes()) {
            BalanceSubstResultNote result = new BalanceSubstResultNote();
            result.setHeader(header);
            result.setMeteringPoint(note.getMeteringPoint());
            result.setLineNum(note.getLineNum());

            if (result.getTranslates() == null)
                result.setTranslates(new ArrayList<>());

            for (BalanceSubstNoteTranslate noteTranslate : note.getTranslates()) {
                BalanceSubstResultNoteTranslate resultTranslate = new BalanceSubstResultNoteTranslate();
                resultTranslate.setNote(result);
                resultTranslate.setLang(noteTranslate.getLang());
                resultTranslate.setNoteText(noteTranslate.getNoteText());
                result.getTranslates().add(resultTranslate);
            }
            results.add(result);
        }
        balanceSubstResultNoteRepo.save(results);
        balanceSubstResultNoteRepo.flush();
    }

    private void addTranslates(BalanceSubstLine line, BalanceSubstResultLine resultLine) {
        if (resultLine.getTranslates() == null)
            resultLine.setTranslates(new ArrayList<>());

        for (BalanceSubstLineTranslate translate : line.getTranslates()) {
            BalanceSubstResultLineTranslate resultTranslate = new BalanceSubstResultLineTranslate();
            resultTranslate.setLang(translate.getLang());
            resultTranslate.setLine(resultLine);
            resultTranslate.setName(translate.getName());
            resultLine.getTranslates().add(resultTranslate);
        }
    }

    private Double getMrVal(BalanceSubstLine bsLine, Parameter param, CalcContext context) {
        MeteringPoint meteringPoint = bsLine.getMeteringPoint();
        if (meteringPoint == null || param == null)
            return null;

        CalcProperty property = CalcProperty.builder()
            .processOrder(ProcessOrderEnum.READ_CALC)
            .build();

        CalcResult result = calcService.calcValue(meteringPoint, param, context, property);
        Double val = result != null ? result.getDoubleValue() : null;
        if (val != null)
            val = val * ofNullable(bsLine.getRate()).orElse(1d);

        return val;
    }

    private Map<String, Parameter> getSections(BalanceSubstLine bLine) {
        Map<String, Parameter> map = new HashMap<>();
        if (bLine.getIsSection1()) map.put("1", paramService.getParam("A+"));
        if (bLine.getIsSection2()) map.put("2", paramService.getParam("A-"));
        if (bLine.getIsSection3()) map.put("3", paramService.getParam("A-"));
        if (bLine.getIsSection4()) map.put("4", paramService.getParam("A-"));
        return map;
    }
}
