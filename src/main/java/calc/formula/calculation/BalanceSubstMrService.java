package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.*;
import calc.entity.calc.bs.mr.*;
import calc.entity.calc.enums.LangEnum;
import calc.formula.CalcContext;
import calc.formula.ContextTypeEnum;
import calc.formula.exception.CalcServiceException;
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
import java.util.*;

import static calc.util.Util.buildMsgParams;
import static calc.util.Util.inverseParam;
import static calc.util.Util.round;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("ImplicitSubclassInspection")
@Service
@RequiredArgsConstructor
public class BalanceSubstMrService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceSubstMrService.class);
    private final BalanceSubstResultMrLineRepo balanceSubstResultMrLineRepo;
    private final BalanceSubstResultBpLineRepo balanceSubstResultBpLineRepo;
    private final MrService mrService;
    private final ParamService paramService;
    private final BalanceSubstResultMrNoteRepo balanceSubstResultMrNoteRepo;
    private final MessageService messageService;
    private static final String docCode = "ACT";

    public boolean calc(BalanceSubstResultHeader header) {
        try {
            logger.info("Act for balance with headerId " + header.getId() + " started");

            CalcContext context = CalcContext.builder()
                .lang(LangEnum.RU)
                .header(header)
                .defContextType(ContextTypeEnum.DEFAULT)
                .build();

            List<BalanceSubstResultMrLine> resultLines = new ArrayList<>();
            for (BalanceSubstMrLine mrLine : header.getHeader().getMrLines()) {
                MeteringPoint meteringPoint = mrLine.getMeteringPoint();
                if (meteringPoint == null)
                    continue;

                Map<String, String> msgParams = buildMsgParams(meteringPoint);
                List<MeteringReading> meteringReadings;
                try {
                    meteringReadings = mrService.calc(meteringPoint, context);
                }
                catch (CalcServiceException e) {
                    msgParams.putIfAbsent("err", e.getMessage());
                    messageService.addMessage(header, mrLine.getId(), docCode, e.getErrCode(), msgParams);
                    continue;
                }

                for (MeteringReading t : meteringReadings) {
                    BalanceSubstResultMrLine line = new BalanceSubstResultMrLine();

                    if (t.getMeter() == null)
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_METER_NOT_FOUND", msgParams);

                    if (t.getMeterHistory() == null)
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_METER_HISTORY_NOT_FOUND", msgParams);

                    String section = getSection(mrLine);
                    if (section == null || section.equals("")) {
                        messageService.addMessage(header, mrLine.getId(), docCode, "MR_SECTION_NOT_FOUND", msgParams);
                        continue;
                    }

                    Parameter param = inverseParam(paramService, t.getParam(), mrLine.getIsInverse());
                    Double val = round(t.getVal(), param);

                    line.setHeader(header);
                    line.setMeteringPoint(meteringPoint);
                    line.setSection(section);
                    line.setBypassMeteringPoint(t.getBypassMeteringPoint());
                    line.setBypassMode(t.getBypassMode());
                    line.setIsBypassSection(t.getIsBypassSection());
                    line.setIsIgnore(false);
                    line.setParam(param);
                    line.setUnit(t.getUnit());
                    line.setMeter(t.getMeter());
                    line.setMeterHistory(t.getMeterHistory());
                    line.setStartMeteringDate(t.getStartMeteringDate());
                    line.setEndMeteringDate(t.getEndMeteringDate());
                    line.setStartVal(t.getStartVal());
                    line.setEndVal(t.getEndVal());
                    line.setDelta(t.getDelta());
                    line.setMeterRate(t.getMeterRate());
                    line.setVal(val);
                    line.setUnderCountVal(t.getUnderCountVal());
                    line.setUndercount(t.getUnderCount());
                    if (meteringPoint.getVoltageClass()!=null)
                        line.setSubSection(meteringPoint.getVoltageClass().getValue().toString());

                    resultLines.add(line);
                }
            }

            deleteLines(header);
            saveLines(resultLines);
            saveBpLines(resultLines);
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
    private void saveLines(List<BalanceSubstResultMrLine> resultLines) {
        balanceSubstResultMrLineRepo.save(resultLines);
        balanceSubstResultMrLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveBpLines(List<BalanceSubstResultMrLine> resultLines) {
        List<BalanceSubstResultBpLine> bpLines = resultLines.stream()
            .filter(t -> t.getBypassMeteringPoint() != null)
            .filter(t -> t.getBypassMode() != null)
            .map(t -> {
                BalanceSubstResultBpLine line = new BalanceSubstResultBpLine();
                line.setHeader(t.getHeader());
                line.setMeteringPoint(t.getMeteringPoint());
                line.setBypassMeteringPoint(t.getBypassMeteringPoint());
                line.setBypassMode(t.getBypassMode());
                line.setMeter(t.getMeter());
                line.setMeterHistory(t.getMeterHistory());
                line.setStartMeteringDate(t.getStartMeteringDate());
                line.setEndMeteringDate(t.getEndMeteringDate());
                line.setStartVal(t.getStartVal());
                line.setEndVal(t.getEndVal());
                line.setDelta(t.getDelta());
                line.setMeterRate(t.getMeterRate());
                line.setVal(t.getVal());
                line.setParam(t.getParam());
                line.setUnit(t.getUnit());
                line.setBypassStartDate(line.getBypassMode().getStartDate());
                line.setBypassEndDate(line.getBypassMode().getEndDate());
                return line;
            })
            .collect(toList());

        balanceSubstResultBpLineRepo.save(bpLines);
        balanceSubstResultBpLineRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void deleteLines(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMrLine> lines = balanceSubstResultMrLineRepo.findAllByHeaderId(header.getId());
        balanceSubstResultMrLineRepo.delete(lines);
        balanceSubstResultMrLineRepo.flush();

        List<BalanceSubstResultMrNote> notes = balanceSubstResultMrNoteRepo.findAllByHeaderId(header.getId());
        balanceSubstResultMrNoteRepo.delete(notes);
        balanceSubstResultMrNoteRepo.flush();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void copyNotes(BalanceSubstResultHeader header) {
        List<BalanceSubstResultMrNote> results = new ArrayList<>();
        for (BalanceSubstMrNote note : header.getHeader().getMrNotes()) {
            BalanceSubstResultMrNote result = new BalanceSubstResultMrNote();
            result.setHeader(header);
            result.setMeteringPoint(note.getMeteringPoint());
            result.setLineNum(note.getLineNum());

            result.setTranslates(Optional.ofNullable(result.getTranslates()).orElse(new ArrayList<>()));
            for (BalanceSubstMrNoteTranslate translate : note.getTranslates()) {
                BalanceSubstResultMrNoteTranslate resultTranslate = new BalanceSubstResultMrNoteTranslate();
                resultTranslate.setNote(result);
                resultTranslate.setLang(translate.getLang());
                resultTranslate.setNoteText(translate.getNoteText());
                result.getTranslates().add(resultTranslate);
            }
            results.add(result);
        }
        balanceSubstResultMrNoteRepo.save(results);
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
}
