package calc.formula.calculation;

import calc.entity.calc.*;
import calc.entity.calc.bs.*;
import calc.entity.calc.enums.PointTypeEnum;
import calc.formula.CalcContext;
import calc.formula.CalcResult;
import calc.formula.ContextType;
import calc.formula.expression.impl.MrExpression;
import calc.formula.expression.impl.PeriodTimeValueExpression;
import calc.formula.service.*;
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
    private final BalanceSubstResultLineRepo balanceSubstResultLineRepo;
    private final ParamService paramService;
    private final BalanceSubstResultMrService mrService;
    private final PeriodTimeValueService periodTimeValueService;
    private final BalanceSubstResultNoteRepo balanceSubstResultNoteRepo;
    private final CalcService calcService;
    private final MessageService messageService;
    private static final String docCode = "BALANCE";
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
                .headerId(header.getId())
                .periodType(header.getPeriodType())
                .startDate(header.getStartDate())
                .endDate(header.getEndDate())
                .orgId(header.getOrganization().getId())
                .energyObjectType("SUBSTATION")
                .energyObjectId(header.getSubstation().getId())
                .contextType(ContextType.MR)
                .values(new HashMap<>())
                .build();

            List<BalanceSubstResultLine> resultLines = new ArrayList<>();
            for (BalanceSubstLine line : header.getHeader().getLines()) {
                MeteringPoint meteringPoint = line.getMeteringPoint();
                if (meteringPoint == null) continue;

                Map<String, Parameter> sections = getSections(line);
                for (String section : sections.keySet()) {
                    Parameter param = line.getParam() == null ? sections.get(section) : line.getParam();
                    param = inverseParam(param, line.getIsInverse());
                    Double val = getMrVal(line, param, context);

                    BalanceSubstResultLine resultLine = new BalanceSubstResultLine();
                    resultLine.setHeader(header);
                    resultLine.setMeteringPoint(meteringPoint);
                    resultLine.setParam(param);
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
            messageService.addMessage(header, null,  docCode,"RUNTIME_EXCEPTION", e.getClass().getCanonicalName());
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

    private Double getMrVal(BalanceSubstLine bsLine, Parameter param, CalcContext context) {
        if (bsLine.getMeteringPoint().getPointType() == PointTypeEnum.PMP) {
            return MrExpression.builder()
                .meteringPointCode(bsLine.getMeteringPoint().getCode())
                .parameterCode(param.getCode())
                .rate(bsLine.getRate())
                .context(context)
                .service(mrService)
                .build()
                .doubleValue();
        }

        Double val = PeriodTimeValueExpression.builder()
            .meteringPointCode(bsLine.getMeteringPoint().getCode())
            .parameterCode(param.getCode())
            .rate(1d)
            .startHour((byte) 0)
            .endHour((byte) 23)
            .periodType(context.getPeriodType())
            .context(context)
            .service(periodTimeValueService)
            .build()
            .doubleValue();

        if (Optional.ofNullable(val).orElse(0d) == 0d) {
            try {
                CalcResult result = calcService.calcMeteringPoint(bsLine.getMeteringPoint(), param, context);
                val = result!=null ? result.getDoubleValue() : null;
                if (val != null)
                    val = val * Optional.ofNullable(bsLine.getRate()).orElse(1d);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return val;
    }

    private Map<String, Parameter> getSections(BalanceSubstLine bLine) {
        Map<String, Parameter> map = new HashMap<>();
        if (bLine.getIsSection1()) map.put("1", mapParams.get("A+"));
        if (bLine.getIsSection2()) map.put("2", mapParams.get("A-"));
        if (bLine.getIsSection3()) map.put("3", mapParams.get("A-"));
        if (bLine.getIsSection4()) map.put("4", mapParams.get("A-"));
        return map;
    }

    private Parameter inverseParam(Parameter param, Boolean isInverse) {
        if (isInverse) {
            if (param.equals(mapParams.get("A+"))) return mapParams.get("A-");
            if (param.equals(mapParams.get("A-"))) return mapParams.get("A+");
            if (param.equals(mapParams.get("R+"))) return mapParams.get("R-");
            if (param.equals(mapParams.get("R-"))) return mapParams.get("R+");
        }
        return param;
    }
}
